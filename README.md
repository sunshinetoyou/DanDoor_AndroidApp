# DanDoor_AndroidApp
| DanDoor | 

Android 기반 BLE Indoor Positioning 정확도 탐지 애플리케이션

해당 프로젝트는 위치 측위 서비스를 사용하기 위한 [안드로이드 라이브러리](https://github.com/sunshinetoyou/DanDoor_AndroidApp/tree/main/library)와 해당 라이브러리를 사용하여 만든 [데모 앱](https://github.com/sunshinetoyou/DanDoor_AndroidApp/tree/main/app/src/main/java/com/dandoor/androidApp)을 제공합니다. ( 데모앱은 미완성 상태입니다. [07-09 기준])

## Demo App
데모앱은 차량을 이용하여 기기의 위치를 이동시켜 비콘의 신호를 수신하여 알고리즘을 통해 예상되는 위치를 계산하여,
실제 위치와 비교하여 정확도를 산출하며, 시각화하여 분석할 수 있도록 제작하고자 하였습니다.  
개발 진행 도중 차량에 문제가 발생하였고, 주어진 기간 안에 해결이 불가능하다고 판단하여 이후에 고안한 두 번째 데모앱은
실험을 진행하면 경로 위에 특정 시점마다 실제 위치와 예상 위치를 화면에 띄워서 즉각적인 확인이 가능하며,
이전에 진행한 실험도 기록을 확인하여 이어서 실험을 진행할 수도 있습니다. 
또한 분석할 lab을 선택하면 실험횟수에 따른 정확도를 그래프로 확인할 수 있습니다.  
예시 UI 혹은 기능들은 [사용자 문서](https://github.com/sunshinetoyou/DanDoor_AndroidApp/blob/main/Document/%EC%82%AC%EC%9A%A9%EC%9E%90%20%EB%AC%B8%EC%84%9C.pdf)에 정리해두었습니다.

## Android Library
기본적으로 `DandoorBTManager`,`DataManager`,`EstimationPluginManager`이 주요 기능들을 담당하고 있습니다.  
`DandoorBTManager`는 블루투스 권한을 확인하여 차량을 제어하고 비콘 신호를 수신하는 기능을 다루며,  
`DataManager`는 데이터들을 관리하는 총괄 매니저 클래스 기능을 합니다.  
`EstimationPluginManager`는 수신되는 rssi값을 이용해 예상되는 위치를 계산하여 평가 및 데이터를 저장하는 기능입니다.  
자세한 내용은 [개발자 문서](https://github.com/sunshinetoyou/DanDoor_AndroidApp/blob/main/Document/%EA%B0%9C%EB%B0%9C%EC%9E%90%20%EB%AC%B8%EC%84%9C.pdf)에 정리해두었습니다.



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
### 버전 관리
ver.1은 main, ver.2는 branch/v2에 있습니다.
