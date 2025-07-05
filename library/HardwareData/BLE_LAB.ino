/*
MOTER 1 : PORT 4연결, 느림
MOTER 2:  PORT 1연결, 빠름 
회전시 고려하는 것도 모터2가 빨라서 오른쪽으로 회전하는 것을 방지함(왼쪽으로 도는 것을 고려 안함)
그래서 회전은 오른쪽으로 돌아야 함.
*/

#define IRSENSOR_PIN A0
#define MOTER1_SPEED 135 // PORT 4 
#define MOTER2_SPEED 90  // PORT 1
#define IRVALUE 400
#define CYCLEDISTANCE 760  // 1바퀴 이동 거리 (cm)

#include <AFMotor.h>
#include <SoftwareSerial.h>

// 블루투스 (BC-06)
SoftwareSerial mySerial(9, 10); // RX, TX

// 모터 설정
AF_DCMotor motor1(4);
AF_DCMotor motor2(1);

// 상태 및 통신 변수
String ms = "";
bool engine_flag = false;
String state = "forward";

// 시간 관련 변수
unsigned long brightStartTime = 0;
unsigned long darkStartTime = 0;
unsigned long startTime = 0;
unsigned long elapsedTime = 0;       // 초 단위
unsigned long lastSendTime = 0;
float carSpeed = 33.0 / CYCLEDISTANCE; // 초당 cm 속도 (1바퀴 33초 기준)

// 회전 횟수
int turningCount = 0;

void setup() {
  mySerial.begin(9600);
  Serial.begin(9600); // 시리얼 모니터 출력용
}

void loop() {
  if (mySerial.available()) {
    ms = mySerial.readStringUntil('c');
    ms.trim();

    if (ms == "start") {
      engine_flag = true;
      state = "forward";
      startTime = millis();       // 주행 시작 시각 저장
      lastSendTime = millis();    // 전송 타이머 초기화
      turningCount = 0;           // 회전 카운터 초기화
    } else if (ms == "stop") {
      engine_flag = false;
      elapsedTime = 0;
    }
    ms = "";
  }

  if (engine_flag) {
    driving();

    // 3초마다 이동 거리 전송
    unsigned long now = millis();
    if (now - lastSendTime >= 3000) {
      elapsedTime = (now - startTime) / 1000;
      float distance = carSpeed * elapsedTime;

      mySerial.print("이동 거리: ");//블루투스로 연결된 기기로 전송
      mySerial.print(distance);
      mySerial.println(" cm");

      lastSendTime = now;
    }
  } else {
    sstop();
  }
}

void driving() {
  int irValue = analogRead(IRSENSOR_PIN);
  unsigned long now = millis();

  if (state == "forward") {
    adjustMotorSpeed();
    motor1.run(FORWARD);
    motor2.run(FORWARD);

    if (irValue > IRVALUE) {  // 밝은 바닥 감지
      if (brightStartTime == 0) brightStartTime = now;
      if (now - brightStartTime >= 3000) {
        state = "turning";
        brightStartTime = 0;
      }
    } else {
      brightStartTime = 0;
    }

  } else if (state == "turning") {
    right();

    if (irValue < IRVALUE) {  // 어두운색 감지되면 전진으로 복귀
      if (darkStartTime == 0) darkStartTime = now;
      if (now - darkStartTime >= 50) {
        state = "forward";
        darkStartTime = 0;
        turningCount++;

        Serial.print("회전 횟수: ");
        Serial.println(turningCount);

        if (turningCount % 4 == 0 && elapsedTime > 0) {
          carSpeed = (float)CYCLEDISTANCE / elapsedTime;  // 새 속도 계산
          startTime = millis();  // 새 거리 측정 시작점으로 갱신
        }
      }
    } else {
      darkStartTime = 0;
    }
  }
}

/* 경로 이탈 방지를 위한 모터 속도 조정 */
void adjustMotorSpeed() {
  int irValue = analogRead(IRSENSOR_PIN);
  if (irValue < IRVALUE) {
    motor1.setSpeed(50);  // 라인 감지 시 속도 줄임
  } else {
    motor1.setSpeed(MOTER1_SPEED);
  }
  motor2.setSpeed(MOTER2_SPEED);
}

/* 오른쪽 회전 */
void right() {
  motor1.setSpeed(0);
  motor2.setSpeed(MOTER2_SPEED);
  motor1.run(BACKWARD);
  motor2.run(FORWARD);
}

/* 정지 */
void sstop() {
  motor1.run(RELEASE);
  motor2.run(RELEASE);
}
