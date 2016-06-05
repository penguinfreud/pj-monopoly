package monopoly.util;

public interface Host {
    <T> void setParasite(Object key, T obj);

    <T> T getParasite(Object key);
}
