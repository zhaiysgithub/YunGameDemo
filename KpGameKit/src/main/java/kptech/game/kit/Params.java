package kptech.game.kit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Params implements Serializable {

    public Map<String, Object> mContainer = new HashMap();

    public Params() {
    }

    public Params(String paramKey, Object value) {
        this.mContainer.put(paramKey, value);
    }

    public boolean isEmpty() {
        return this.mContainer.isEmpty();
    }

    public Params(Params other) {
        if (other != null && !other.isEmpty()) {
            this.mContainer.putAll(other.mContainer);
        }
    }

    public boolean contains(String key) {
        return this.mContainer.containsKey(key);
    }

    public Params put(String key, Object value) {
        this.mContainer.put(key, value);
        return this;
    }

    public Params putAll(Map<String, Object> map) {
        this.mContainer.putAll(map);
        return this;
    }

    public Iterator<Map.Entry<String, Object>> iterator() {
        if (this.mContainer != null) {
            Iterator<Map.Entry<String, Object>> iterator = this.mContainer.entrySet().iterator();
            return iterator;
        } else {
            return null;
        }
    }

    public <T> T get(String key, T def) {
        T ret = (T) this.mContainer.get(key);
        if (ret == null) {
            ret = def;
        }

        return ret;
    }

    public String toString() {
        return this.mContainer.toString();
    }

    public static <T> T get(Params params, String key, T def) {
        return params == null ? def : params.get(key, def);
    }

    public void remove(String key) {
        this.mContainer.remove(key);
    }
}
