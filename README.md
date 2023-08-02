# Springboot URL Shortener
ReviewSolicitors originally developed this project  to test my skill during a live coding test. I secured the Job and I decided to improve the code.

## Purpose
The purpose of this project is to shorten long URLs and handle the redirection of short URLs to their corresponding long URLs. 

## Tech Stack
- Postgresql
- JDBCTemplate
- Springboot
- Maven
- Java 17
- Spring Security
- Google Zxing for Barcode generation

## Features
1. Encode URLs using base 64
2. Decode URLs
3. Save Short and long URLs to the database
4. Look up a short URL in the database and map it to the Long URL
5. Update Function: Allow updating the original URL associated with a short URL.
6. Delete Function: Allow deleting a short URL and its associated original URL from the database.
7. URL Statistics function to track and display statistics about the usage of short URLs, such as the number of times a short URL has been accessed.
8. Expiration: Implement an expiration mechanism for short URLs, so they become invalid after a certain period.
9. Enhanced the URL validation to ensure that only valid URLs can be shortened and stored.
10. Block the user after 3 attempts to look up a short URL by tracking their IP address
11. User Authentication: Add user authentication to track URLs and their statistics on a per-user basis.
12. QR Codes: Generate QR codes for short URLs to make it easy to share them in printed materials or other media.
13. Encode with Padding: Currently, the encode method does not add any padding to the generated short URL. To make all short URLs have a fixed length, you can add padding to the encoded result. This ensures that the length of the short URL remains consistent, making it more aesthetically pleasing and easier to work with.
14. Decode with Padding: To support decoding of short URLs with padding, I added a method to handle the padding and decode the short URL back to its original long URL.
15. Custom Character Set: If you want to use a different character set for encoding, you can add a method to set a custom character set for the Base62 encoding. This could be useful if you want to generate URLs with a smaller character set, excluding certain characters that might cause issues in certain contexts.
16. Security Considerations: CSRF protection is added to prevent cross-site request forgery attacks.

