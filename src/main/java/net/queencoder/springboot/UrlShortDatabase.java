package net.queencoder.springboot;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class UrlShortDatabase {
	
	@Autowired
	private JdbcTemplate jdbctemplate;
	
	
	@PostConstruct
	public void constructDB() {
		//jdbctemplate.execute("CREATE TABLE If Not Exists urls (short_url varchar(32) Primary Key, long_url varchar(256));");
		
		// Create the URL statistics table if not exists
		jdbctemplate.execute("CREATE TABLE IF NOT EXISTS url_statistics (short_url VARCHAR(32) PRIMARY KEY, access_count BIGINT);");
		
		// Create the URL expiration table if not exists
		jdbctemplate.execute("CREATE TABLE IF NOT EXISTS urls (short_url VARCHAR(32) PRIMARY KEY, long_url VARCHAR(256), expiration_timestamp TIMESTAMP);");
	}
	
	
	public void insert(String base62Number, String longUrl) {	
		jdbctemplate.update("INSERT INTO urls (short_url, long_url) Values (?,?)", base62Number, longUrl);
	}

	
	public String lookupByShortUrl(String shortUrl) {
		List<String> results = jdbctemplate.queryForList("SELECT long_url FROM urls WHERE short_url = ?", String.class, shortUrl);
		if(results.isEmpty()){
			return null;
		}
		return results.get(0);
	}
	
	public void incrementAccessCount(String shortUrl) {
		jdbctemplate.update("INSERT INTO url_statistics (short_url, access_count) VALUES (?, 1) ON DUPLICATE KEY UPDATE access_count = access_count + 1", shortUrl);
    }

    public Long getAccessCount(String shortUrl) {
        Long accessCount = jdbctemplate.queryForObject("SELECT access_count FROM url_statistics WHERE short_url = ?", Long.class, shortUrl);
        return accessCount != null ? accessCount : 0L;
    }
    
    public void insertExpirationTimestamp(String base62Number, String longUrl, Instant expirationTime) {    
    	jdbctemplate.update("INSERT INTO urls (short_url, long_url, expiration_timestamp) VALUES (?,?,?)", base62Number, longUrl, Timestamp.from(expirationTime));
    }
    
    public boolean isShortUrlExpired(String shortUrl) {
        Timestamp expirationTimestamp = jdbctemplate.queryForObject("SELECT expiration_timestamp FROM urls WHERE short_url = ?", Timestamp.class, shortUrl);
        if (expirationTimestamp != null) {
            return expirationTimestamp.toInstant().isBefore(Instant.now());
        }
        return false; // Short URL does not exist or has no expiration time
    }

	
	public void update(String shortUrl, String newLongUrl) {
		jdbctemplate.update("UPDATE urls SET long_url = ? WHERE short_url = ?", newLongUrl, shortUrl);
    }

    public void delete(String shortUrl) {
    	jdbctemplate.update("DELETE FROM urls WHERE short_url = ?", shortUrl);
    }
	
}
