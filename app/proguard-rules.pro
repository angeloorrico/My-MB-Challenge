# Retrofit, OkHttp, Moshi codegen and Hilt all ship their own consumer ProGuard rules inside
# their AARs, so no manual keep rules are needed for them here.

# Moshi-generated adapters reference DTOs by reflection-free generated code, but keep DTO class
# names anyway so JSON field mapping stays stable if reflection-based fallback is ever added.
-keep class com.aorrico.mymbchallenge.data.remote.dto.** { *; }
