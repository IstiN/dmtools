You can use only the list of frameworks from existing dependencies

```
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.mockito:mockito-core:5.14.2'
    testImplementation 'org.mockito:mockito-inline:5.2.0'
    
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.2")

    api 'org.json:json:20231013'
    api 'com.squareup.okhttp3:okhttp:4.12.0'
    api 'org.apache.commons:commons-lang3:3.8.1'

    api 'com.thedeanda:lorem:2.1'

    api 'org.apache.logging.log4j:log4j-api:2.17.0'
    api 'org.apache.logging.log4j:log4j-core:2.17.1'

    api group: 'commons-io', name: 'commons-io', version: '2.14.0'
    api group: 'commons-codec', name: 'commons-codec', version: '1.13'
    api group: 'org.freemarker', name: 'freemarker', version: '2.3.30'

    api(group: 'org.jsoup', name: 'jsoup', version: '1.17.2')

    api 'org.apache.logging.log4j:log4j-api:2.20.0'
    api 'org.apache.logging.log4j:log4j-core:2.20.0'
    api 'org.apache.logging.log4j:log4j-slf4j-impl:2.20.0'

    api group: 'org.jxls', name: 'jxls', version: '2.14.0'
    api group: 'org.jxls', name: 'jxls-poi', version: '2.14.0'
    api group: 'org.jxls', name: 'jxls-reader', version: '2.1.0'

    api(group: 'org.apache.commons', name: 'commons-collections4', version: '4.3')
    api(group: 'org.apache.commons', name: 'commons-jexl', version: '2.1.1')

    api group: 'org.apache.poi', name: 'poi', version: '4.1.1'
    api group: 'org.apache.poi', name: 'poi-ooxml', version: '3.17'

    api group: 'org.slf4j', name: 'slf4j-simple', version: '1.7.5'
    api 'org.apache.pdfbox:pdfbox:3.0.2'
    api 'io.github.furstenheim:copy_down:1.1'
    api 'org.seleniumhq.selenium:selenium-java:4.21.0'
    api 'io.github.bonigarcia:webdrivermanager:5.9.2'
    // https://mvnrepository.com/artifact/org.graalvm.js/js
    api group: 'org.graalvm.js', name: 'js', version: '24.1.1'
    api group: 'org.graalvm.js', name: 'js-scriptengine', version: '24.1.1'

    // https://mvnrepository.com/artifact/com.github.mpkorstanje/simmetrics
    api 'com.github.mpkorstanje:simmetrics-core:4.1.1'
```

You must avoid exceptions like:
```
The used MockMaker SubclassByteBuddyMockMaker does not support the creation of static mocks

Mockito's inline mock maker supports static mocks based on the Instrumentation API.
You can simply enable this mock mode, by placing the 'mockito-inline' artifact where you are currently using 'mockito-core'.
Note that Mockito's inline mock maker is not supported on Android.
org.mockito.exceptions.base.MockitoException: 
The used MockMaker SubclassByteBuddyMockMaker does not support the creation of static mocks

Mockito's inline mock maker supports static mocks based on the Instrumentation API.
You can simply enable this mock mode, by placing the 'mockito-inline' artifact where you are currently using 'mockito-core'.
Note that Mockito's inline mock maker is not supported on Android.
```

You must avoid exceptions like:
```
is a *void method* and it *cannot* be stubbed with a *return value*!
Voids are usually stubbed with Throwables:
doThrow(exception).when(mock).someVoidMethod();
If you need to set the void method to do nothing you can use:
doNothing().when(mock).someVoidMethod();
For more information, check out the javadocs for Mockito.doNothing().
***
If you're unsure why you're getting above error read on.
Due to the nature of the syntax above problem might occur because:
1. The method you are trying to stub is *overloaded*. Make sure you are calling the right overloaded version.
2. Somewhere in your test you are stubbing *final methods*. Sorry, Mockito does not verify/stub final methods.
3. A spy is stubbed using when(spy.foo()).then() syntax. It is safer to stub spies -
    - with doReturn|Throw() family of methods. More in javadocs for Mockito.spy() method.
4. Mocking methods declared on non-public parent classes is not supported.
```