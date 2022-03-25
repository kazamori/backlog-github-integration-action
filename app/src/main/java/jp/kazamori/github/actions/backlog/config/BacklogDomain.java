package jp.kazamori.github.actions.backlog.config;

import com.nulabinc.backlog4j.conf.BacklogComConfigure;
import com.nulabinc.backlog4j.conf.BacklogConfigure;
import com.nulabinc.backlog4j.conf.BacklogJpConfigure;
import com.nulabinc.backlog4j.conf.BacklogToolConfigure;
import lombok.val;

import java.net.MalformedURLException;

public enum BacklogDomain {
    BACKLOG_COM("backlog.com") {
        @Override
        BacklogConfigure get(String spaceKey, String apiKey) throws MalformedURLException {
            return new BacklogComConfigure(spaceKey).apiKey(apiKey);
        }
    },

    BACKLOG_TOOL("backlogtool.com") {
        @Override
        BacklogConfigure get(String spaceKey, String apiKey) throws MalformedURLException {
            return new BacklogToolConfigure(spaceKey).apiKey(apiKey);
        }
    },

    BACKLOG_JP("backlog.jp") {
        @Override
        BacklogConfigure get(String spaceKey, String apiKey) throws MalformedURLException {
            return new BacklogJpConfigure(spaceKey).apiKey(apiKey);
        }
    };

    private final String domainName;

    BacklogDomain(String domainName) {
        this.domainName = domainName;
    }

    public String getDomainName() {
        return this.domainName;
    }

    abstract BacklogConfigure get(String spaceKey, String apiKey) throws MalformedURLException;

    public static BacklogDomain fromDomainName(String domainName) {
        for (var config : BacklogDomain.values()) {
            if (config.getDomainName().equalsIgnoreCase(domainName)) {
                return config;
            }
        }
        throw new IllegalArgumentException(String.format("Not supported domain: %s", domainName));
    }

    public static BacklogConfigure create(String fqdn, String apiKey) throws MalformedURLException {
        val domainNames = fqdn.split("\\.");
        if (domainNames.length != 3) {
            throw new IllegalArgumentException(String.format("Invalid domain name: %s", fqdn));
        }
        val spaceKey = domainNames[0];
        var config = BacklogDomain.fromDomainName(String.format("%s.%s", domainNames[1], domainNames[2]));
        return config.get(spaceKey, apiKey);
    }
}
