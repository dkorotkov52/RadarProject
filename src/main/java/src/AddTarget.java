package src;


@FunctionalInterface
public interface AddTarget {

    Target apply(int angle, double distance, Runnable callback);

}
