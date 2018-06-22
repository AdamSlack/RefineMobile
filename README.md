# Koala Hero
## Clean Slate.
The original demo app is being all but removed. The elements from the app that remain are those relating to consuming back end services.

### XRay API

API File: `XRayAPI`

Parser File: `XRayJsonParser`

Data Classes: `XRayAppInfo`, `XRayAppStoreInfo`


### CSM API

API File: `CSMAPI`

Parser File: `CSMJsonParser`

Data Classes: `CSMAppInfo`, `CSMParentalGuidance`


### Tracker Mapper API

API File: `TrackerMapperAPI`

Parser File: `TrackerMapperJsonParser`

Data Classes: `TrackerMapperCompany`

### Usage of Each API Component.

Each API is consumed in the same way. A class extending `AsyncTask` is implemented, and instances of it provide two `Function` objects as well as a `Context` object. The first `Function` represents what is to happen after the task is complete (`onPostExecute`), the second is for what happens during a progress update for the task (`onProgressUpdate`).

Example Usage of XRayAPI.
```java
new XRayAPI.XRayAppData(
  new Function<Void, Void>(){
    @Override
    public Void apply(Void nothing){
      // On Post Execute, this Function is applied.
      return null;
    }
  },
  new Function<XRayAppInfo, Void>() {
    @Override
    public Void apply(XRayAppInfo appInfo){
      // On Progress Update, this Function is applied.
      return null;
    }
  },
  getApplicationContext()
).execute("com.linkedin.android","com.whatsapp","com.tencent.mm");
```
