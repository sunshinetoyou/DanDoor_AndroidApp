# DanDoor_AndroidApp
| DanDoor | 

Android 기반 BLE Indoor Positioning 정확도 탐지 애플리케이션

해당 프로젝트는 위치 측위 서비스를 사용하기 위한 안드로이드 라이브러리와 해당 라이브러리를 사용하여 만든 데모 앱을 제공합니다. ( 데모앱은 미완성 상태입니다. [07-09 기준])

## Demo App



## Android Library




### Android Library 사용 방법

해당 라이브러리는 jitpack 을 통해 배포되었습니다. 

```
// setting.gradle.kts에 jitpack 추가
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		mavenCentral()
		maven { url = uri("https://jitpack.io") }
	}
}

// build.gradle.kts에 의존성 추가
dependencies {
	implementation("com.github.User:Repo:Tag")
}
```
