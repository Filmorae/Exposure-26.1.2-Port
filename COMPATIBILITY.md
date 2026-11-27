# Verified compatibility matrix

This table records controlled verification only. An entry does not imply support for different versions, GPUs, mod combinations, or Shader Packs.

| Component or environment | Verified version/state | Scope |
|---|---|---|
| Minecraft | 26.1.2 | Required target |
| Java | 25 | Compile, tests, client, and server gates |
| Fabric Loader | 0.19.3 | Client and dedicated server |
| Fabric API | 0.150.0+26.1.2 | Build and controlled runtime baseline |
| Fabric API | 0.151.0+26.1.2 | Controlled runtime compatibility |
| Forge Config API Port | 26.1.4 | Required runtime dependency |
| No Shader Pack | Verified | Direct/background Capture and core rendering |
| Iris | 1.11.2 | Controlled Shader Capture matrix only |
| Sodium | 0.9.1 | Controlled Shader Capture matrix only |
| BSL | 8.4.01.2 | Controlled Iris/Sodium Capture matrix only |
| Integrated server | Verified | World entry, persistence, menus, commands, capture/export scenarios |
| Dedicated server | Verified | Startup to `Done`, server-safe paths, persistence/export scenarios, normal stop |

Everything not listed is unverified unless a later dated entry or community report documents the exact environment and reproduction. Community reports should be labeled as such and should not replace controlled verification.

## 2026-11-27 world-frame capture candidate

Controlled on Minecraft 26.1.2, Java 25, Fabric Loader 0.19.3, Fabric API 0.150.0+26.1.2 and Forge Config API Port 26.1.4.

| Scenario | Expected | Observed |
|---|---|---|
| Photon v1.4 with Iris 1.10.9, Sodium 0.8.12, RenderScale 1.4.0-alpha.4 at 70%, Mesa llvmpipe and `SH_SKYLIGHT=false` | The raw frame retains the live sky and terrain | Three consecutive 1/60 captures retained the clouds at the first rendered world frame. The previously reproduced texture-copy bounds error did not recur. |
| Same shader configuration, real hand-held camera and photograph filter | The shutter uses the first eligible world frame and the filter uses its own target | A hand-held shutter and a filter capture both requested the first rendered frame. The filter used a separate color target and exported successfully. |
| Same shader configuration, two-second Camera Stand timer, player 18 blocks from the stand and looking in the opposite direction | A complete stand frame reaches the film while the display keeps the player view | The first frame at the new viewpoint initially lacked terrain. With a single settling frame, repeated captures showed the stand's red target and the presented frame retained the player's green target. The client camera returned to the player before presentation, and the timed exposure was exported from its film frame identifier. |
| Same Camera Stand scene with shaders disabled | The timer, selected viewpoint and presentation behave the same way | The captured frame showed the stand's target while the display kept the player's view and the camera returned to the player. |

The complete Gradle build, both access widener validations and all 60 migration tests passed.
The exact unmodified Photon configuration exceeds llvmpipe's compute shared-memory limit. IterationRP and production GPU frame times still require validation.
