# Software Supply Chain Notice
This project makes extensive use of the Android platform and the [GDAL](https://gdal.org/) project

# Open Source?
While each of these dependencies are conventionally "open source", their implementation on a user device cannot be guaranteed to be faithful to the original source code. A compromised Android platform or GDAL binary blob could theoretically leak sensitive information if such a device is connected to the internet. For this reason, this software should not be used with devices that will be regularly connected to the internet

# Non-FOSS, non-transparent binary blobs
To expedite development, this app uses non-transparent "binary blobs" that have been compiled from GDAL open source code by another developer (GitHub user [eltorio](https://github.com/eltorio) from [their fork of the RtkGPS project](https://github.com/eltorio/RtkGps/tree/e87f9a3088f9179f40d0714f2e54f73778a1c49f))

Given the nature of these pre-compiled files, I am unable to verify their security and full authenticity. However: I provide below sha256 file checksums so any consumer of this product may verify that these "binary blob" files have not been altered (since the beginning of this project's development) if this GitHub repository is compromised by state-actors with full legal authority to do so

Please verify each hash is identical to my corresponding tweet. This repo may be compromised at a future time, yet I cannot be compelled to falsibly certify a compromised version

`bobjoe@mighty-m1 OpenAthenaAndroid % find ./gdal-prebuilt -type f \( -name "libgdal.a" -o -name "libgdal.so" \) -print0 | xargs -0 sha256sum`
[`6bfb2c51893c26f799083163f38d34571fabebc6605b525e933558398f6ca621  ./gdal-prebuilt/armeabi-v7a/lib/libgdal.a`](https://twitter.com/Matts_Bytes/status/1539354022909554689?s=20&t=KeIEwrubv-hIEhRAMzGUdA)
[`7350f5db0b59e5b2fc41be81004591c4058de73782069a1f342642edda84c411  ./gdal-prebuilt/armeabi-v7a/lib/libgdal.so`](https://twitter.com/Matts_Bytes/status/1539354123556073472?s=20&t=KeIEwrubv-hIEhRAMzGUdA)
[`7436556f08856948a8d4ecc4133c6cc165a7911117bf838661afc0135f9f589d  ./gdal-prebuilt/x86/lib/libgdal.a`](https://twitter.com/Matts_Bytes/status/1539354373700169730?s=20&t=KeIEwrubv-hIEhRAMzGUdA)
[`6d7a7f088d29eba8d1eeeaf5475e7347ac9a5236fc228d11da492ccc923b4ed8  ./gdal-prebuilt/x86/lib/libgdal.so`](https://twitter.com/Matts_Bytes/status/1539354512649068545?s=20&t=KeIEwrubv-hIEhRAMzGUdA)
[`f7ce6fd68f7967e7fd12a07cb11c9cba18bd0603460339c6c4faf80bcddd0a35  ./gdal-prebuilt/arm64-v8a/lib/libgdal.a`](https://twitter.com/Matts_Bytes/status/1539354621008912386?s=20&t=KeIEwrubv-hIEhRAMzGUdA)
[`7e48d2038c961c11470aa0619458332df9394ab8124f8a6ceb433999d62f1889  ./gdal-prebuilt/arm64-v8a/lib/libgdal.so`](https://twitter.com/Matts_Bytes/status/1539354981576454153?s=20&t=KeIEwrubv-hIEhRAMzGUdA)
[`366dbf765ca11bb806792b990fe1ac879337322377b8d6a632b3f4acf7f5e83d  ./gdal-prebuilt/x86_64/lib/libgdal.a`](https://twitter.com/Matts_Bytes/status/1539355097217519618?s=20&t=KeIEwrubv-hIEhRAMzGUdA)
[`2af2e8fd8b24949c0b000974632472b161e83e17b4e4c2414b470d6d2e570840  ./gdal-prebuilt/x86_64/lib/libgdal.so`](https://twitter.com/Matts_Bytes/status/1539355323978350592?s=20&t=KeIEwrubv-hIEhRAMzGUdA)


