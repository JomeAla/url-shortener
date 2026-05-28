package com.jomea.urlshortener.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;

@Service
public class DnsLookupService {

    private static final Logger log = LoggerFactory.getLogger(DnsLookupService.class);

    public List<String> lookupTxtRecords(String domain) {
        List<String> records = new ArrayList<>();
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            DirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(domain, new String[]{"TXT"});
            if (attrs != null && attrs.get("TXT") != null) {
                for (int i = 0; i < attrs.get("TXT").size(); i++) {
                    String raw = (String) attrs.get("TXT").get(i);
                    records.add(raw);
                }
            }
            ctx.close();
        } catch (NamingException e) {
            log.warn("DNS lookup failed for {}: {}", domain, e.getMessage());
        }
        return records;
    }

    public boolean verifyTxtRecord(String domain, String expectedValue) {
        List<String> records = lookupTxtRecords(domain);
        for (String record : records) {
            String stripped = record.replace("\"", "").trim();
            if (stripped.equals(expectedValue)) {
                return true;
            }
        }
        return false;
    }
}
