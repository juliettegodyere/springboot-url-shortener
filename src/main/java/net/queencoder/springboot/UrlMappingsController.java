package net.queencoder.springboot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.io.OutputStream;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.net.MalformedURLException;
import java.net.URL;

import jakarta.servlet.http.HttpServletRequest;

// ...

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class UrlMappingsController {
	
	private static final Logger log = LogManager.getLogger();
	
	@Autowired
	private UrlShortDatabase shortUrlDb;
	
	private static Map<String, Integer> ipCount = new HashMap<>();
	private static int maxCount = 3;



	// --------------------------------------------------------------------------------------------------------------------- //
	// The first set of methods are just here to illustrate how a Spring MVC application can work. The @RequestMapping 
	// annotation is what tells Spring that when a certain URL is requested in a browser it should be mapped onto a specific
	// method here. Spring has several convenient mechanisms for (eg) mapping querystring paramters to method variables, or
	// passing in a Model object, or even HttpServletRequest and HttpServletResponse objects, as variables.
	// You should NOT need to change these methods.
	// --------------------------------------------------------------------------------------------------------------------- //
	
	@RequestMapping("/") // defines the URL to access this method - so this will be http://localhost:8080/
	public String hello(
			@RequestParam(defaultValue="World") String who,  // querystring or form parameter with name "who"
			Model model // the Model object is provided by the Spring framework to allow you to pass variables to the template
			) {
		model.addAttribute("who", who);
		return "hello"; // resolves to the HTML template at src/main/resources/templates/hello.html
	}

	
	@RequestMapping("/sam") // defines the URL to access this method - so this will be http://localhost:8080/sam
	public String helloSam() {
		return "redirect:/?who=Sam" ; // using the "redirect:" prefix tells Spring to return a 301 redirect instead of rendering content
	}

	
	@RequestMapping("/sam/{surname}") // defines the URL to access this method - so this will be http://localhost:8080/sam/someValue
	public String helloSamWithSurname(
			@PathVariable(value="surname") String surname // this variable's value is obtained from the RequestMapping, for
				// example if the URL "/sam/foo" was requested the variable's value would be "foo" 
		) {
		return "redirect:/?who=Sam+" + surname; // so eg. http://localhost:8080/sam/I+am would redirect to http://localhost:8080/?who=Sam+I+am
	}
	
	
	@RequestMapping("favicon.ico") // browsers insist on requesting the favicon so just return a 404
	public void favicon(HttpServletResponse response) throws IOException {
		//response.sendError(404);
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	
	// --------------------------------------------------------------------------------------------------------------------- //
	// The next set of methods are the actual useful thing that do the work of the application.
	// Here is where you may need to add/remove/change code in order to complete the coding task. 
	// --------------------------------------------------------------------------------------------------------------------- //
	
	// Shorten a URL and then show an HTML page
	@RequestMapping("/html/shorten")
	public String shortenUrlAndReturnHtml(@RequestParam(value="url", required=true) String longUrl, Model model) {
		//URL Validation: Enhance URL validation to ensure that only valid URLs can be shortened and stored.
		if(!isValidUrl1(longUrl)) {
			model.addAttribute("error", "Invalid URL");
            return "error";
		}

		String shortUrl = shortenUrl(longUrl);
		
		model.addAttribute("originalUrl", longUrl);
		model.addAttribute("shortUrl", "http://localhost:8080/" + shortUrl);
		
		return "shortened";
	}
	
	
	// Shorten a URL and then return a JSON payload
	@RequestMapping("/json/shorten")
	public @ResponseBody Map<String, String> shortenUrlAndReturnJson(@RequestParam(value="url", required=true) String longUrl, Model model) {
		//URL Validation: Enhance URL validation to ensure that only valid URLs can be shortened and stored.
		if (!isValidUrl1(longUrl)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid URL");
            return errorResponse;
        }
		String shortUrl = shortenUrl(longUrl);
		
		Map<String, String> payload = new HashMap<>();
		payload.put("originalUrl", longUrl);
		payload.put("shortUrl", "http://localhost:8080/" + shortUrl);
		
		return payload;
	}

	
	private String shortenUrl(String longUrl) {
		
//		Random randomNumber = new Random();
//		long random = randomNumber.nextLong();
        long random = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
		
		if(random < 0) {
			random = random * -1;
		}
		String shortUrl = Base62.encode(random);
		shortUrlDb.insert(shortUrl, longUrl);
		
		return shortUrl;
	}
	
	
	@RequestMapping("/{shortUrl}")
	public String mapUrl(@PathVariable(value="shortUrl") String shortUrl, Model model, HttpServletRequest request) {
		
		String longUrl = shortUrlDb.lookupByShortUrl(shortUrl);
		
		if(longUrl == null){
			log.info("Short url code [{}] not found in DB so returning no content", shortUrl);
			//longUrl = "";
			model.addAttribute("error", "Short url code [{}] not found in DB so returning no content");
			return "error";
		}
		 // Decode the short URL to obtain the original numeric value
        //long decodedValue = Base62.decode(shortUrl);

        // Add the decoded value to the model to display it in the view
        //model.addAttribute("decodedValue", decodedValue);
		// Check if the short URL has expired
	    if (shortUrlDb.isShortUrlExpired(shortUrl)) {
	        model.addAttribute("error", "This short URL has expired.");
	        return "error";
	    }
		String ipAddress = request.getRemoteAddr();
		log.info(ipAddress);
		// Increment access count for the short URL and save to the DB
        shortUrlDb.incrementAccessCount(shortUrl);
		
        //This tracks the usage of the URL by monitoring the number of times an IP has accessed the ShortURL.
		if(getCurrentValue(ipAddress) > maxCount) {
			model.addAttribute("error", "You have been blocked because you have exceeded the minimum use.");
			log.info("Your blocked");
		}else {
			incrementAccessCount(ipAddress);
		}
				
		return "redirect:" + longUrl;
	}
	
	public void incrementAccessCount(String clientIP) {
		int currentCount = ipCount.getOrDefault(clientIP, 0);
		ipCount.put(clientIP, currentCount+1);
	}
	
	public Integer getCurrentValue(String clientIP) {
		return ipCount.getOrDefault(clientIP, 0);
	}
	
	
	@RequestMapping("/stats/{shortUrl}")
    public String showStatistics(@PathVariable(value = "shortUrl") String shortUrl, Model model) {
        Long accessCount = shortUrlDb.getAccessCount(shortUrl);
        model.addAttribute("shortUrl", shortUrl);
        model.addAttribute("accessCount", accessCount);

        return "statistics";
    }

	private boolean isValidUrl1(String url) {
	    try {
	        new URL(url);
	        return true;
	    } catch (MalformedURLException e) {
	        return false;
	    }
	}
	
	@RequestMapping("/qrcode/{shortUrl}")
	public void generateQRCode(@PathVariable(value = "shortUrl") String shortUrl, HttpServletResponse response) throws IOException {
	    String qrCodeData = "http://localhost:8080/" + shortUrl;
	    int size = 250;
	    String fileType = "png";

	    try {
	        Map<EncodeHintType, Object> hints = new HashMap<>();
	        hints.put(EncodeHintType.MARGIN, 0);

	        QRCodeWriter qrCodeWriter = new QRCodeWriter();
	        BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, size, size, hints);

	        OutputStream outputStream = response.getOutputStream();
	        response.setContentType("image/" + fileType);
	        StreamUtils.copy(bitMatrixToByteArray(bitMatrix, fileType), outputStream);
	        outputStream.close();
	    } catch (WriterException e) {
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    }
	}

	private byte[] bitMatrixToByteArray(BitMatrix bitMatrix, String imageFormat) throws IOException {
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    MatrixToImageWriter.writeToStream(bitMatrix, imageFormat, outputStream);
	    return outputStream.toByteArray();
	}

	
	@RequestMapping("/update")
    public String updateUrl(
            @RequestParam(value = "shortUrl", required = true) String shortUrl,
            @RequestParam(value = "newLongUrl", required = true) String newLongUrl,
            Model model) {

        // Validate input URL
        if (!isValidUrl1(newLongUrl)) {
            model.addAttribute("error", "Invalid URL");
            return "error";
        }

        shortUrlDb.update(shortUrl, newLongUrl);
        return "update_success"; // Display a success page
    }
	// Add this new method to handle CSRF error
    @RequestMapping("/csrf-error")
    public String csrfError() {
        return "csrf-error"; // Create a new "csrf-error.html" template to display CSRF error message
    }

    @RequestMapping("/delete")
    public String deleteUrl(
            @RequestParam(value = "shortUrl", required = true) String shortUrl,
            Model model) {

        shortUrlDb.delete(shortUrl);
        return "delete_success"; // Display a success page
    }
    private boolean isValidUrl(String url) {
        // Simple URL validation using regular expression
        // You can use more sophisticated URL validation libraries in real applications.
        String urlRegex = "^(http|https)://[a-zA-Z0-9\\-.]+\\.[a-zA-Z]{2,}(?:/[^\\s]*)?$";
        return url.matches(urlRegex);
    }
	
	
	// You can add more methods here if you need to!
	
}
