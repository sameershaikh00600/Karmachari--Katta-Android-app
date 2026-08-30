plugins {
    alias(libs.plugins.android.application) apply false
    // ADD THIS LINE:
    id("com.google.gms.google-services") version "4.5.0" apply false
}