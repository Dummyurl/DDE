package pratham.dde.utils;

public interface PermissionResult {

    void permissionGranted();

    void permissionDenied();

    void permissionForeverDenied();

}
