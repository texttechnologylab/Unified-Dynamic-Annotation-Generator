package uni.textimager.sandbox.api.service;

import java.util.Map;

public interface DataProvider {
    Map<String, Object> lookup(String type, String id);
}
