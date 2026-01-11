# Solace Java RTO IPC - Example (Windows)

## What this project is
* Minimal Gradle project demonstrating Solace Java RTO IPC (peer-to-peer) server and client.
* **This project does NOT include Solace binaries** due to licensing. You must download the Java RTO SDK (solclientj.jar) and Windows native DLLs from Solace and place them in the `libs/` and `native/` folders respectively.

## Prerequisites
1. Java 17 (64-bit)
2. IntelliJ IDEA or any IDE that supports Gradle
3. Download the **Solace Java RTO** package (Java + IPC add-on) from Solace support.
   - You need `solclientj.jar` --> put into `libs/`
   - You need native DLLs (`solclient.dll`, `solclientj.dll`) --> put into `native/`
   - Ensure DLL bitness (x64) matches your JVM.

## Project layout
```
solace-ipc-rto/
├─ build.gradle
├─ settings.gradle
├─ libs/             <-- put solclientj.jar here
├─ native/           <-- put solclient.dll, solclientj.dll here
└─ src/main/java/com/ipc/
   ├─ IPCServer.java
   └─ IPCClient.java
```

## How to run (IntelliJ)
1. Copy `solclientj.jar` into `libs/`.
2. Copy `solclient.dll` and `solclientj.dll` into `native/`.
3. Open the project in IntelliJ (Open > select folder).
4. Build the project (Gradle -> Refresh).
5. Run `com.ipc.IPCServer` (Run icon or Run Configuration).
6. Run `com.ipc.IPCClient` (in a new terminal / Run configuration).

## Notes
* The project will compile only if `solclientj.jar` is present in `libs/`.
* The application will run only if the native DLLs are present in `native/` and JVM bitness matches them.
* This example uses plain direct (unreliable) IPC; guaranteed messaging is not supported in IPC.

## Troubleshooting
* `UnsatisfiedLinkError` -> native DLLs not found or wrong bitness.
* `NoClassDefFoundError` or missing ContextHandle -> wrong solclientj.jar or missing it.
