#define IRSENSOR_PIN A0
#define STRIGHT_SEC 3000
#define TURN_SEC    500

#include <AFMotor.h>
#include <SoftwareSerial.h>

/* BC-06 */
SoftwareSerial mySerial(9, 10); // RX, TX

/* motor 설정 (쉴드 1, 4 사용)*/
AF_DCMotor motor1(4);
AF_DCMotor motor2(1);

/* 블루투스를 통한 엔진 제어 메시지, 플래그 */
String ms = "";
bool engine_flag = false;

/* 
  패스 단계 (직진, 우회전 2가지 | 정사각형 기준)
  패스 시작 시작(하나의 행동의 기준 시간점, 중간중간 초기화함)
*/
int pathStep = 0;
unsigned long pathStartTime = 0;

/* 좌우 모터 출력 조정 (경로 삐꾸나면 다시 경로로 돌아오게)*/
void adjustMotorSpeed() {
  int irValue = analogRead(IRSENSOR_PIN);
  
  if (irValue < 500) {
    motor1.setSpeed(0);
  } else {
    motor1.setSpeed(200);
  }
  motor2.setSpeed(160);
}

/* 초기 설정 */
void setup() {
  mySerial.begin(9600);

  pathStartTime = millis();
}

void loop() {
  /* 블루투스 입력 */
  if (mySerial.available()) {
    ms = mySerial.readStringUntil('c');
    ms.trim();

    if (ms == "start") {
      engine_flag = true;
      pathStep = 0;
      pathStartTime = millis();
    }
    else if (ms == "stop") {
      engine_flag = false;
    }
    /* 메시지 버퍼 초기화 */
    ms = "";
  }

  /* 엔진 플래그 기준 운전/스탑 */
  if (engine_flag) {
    driving();
  } else {
    sstop();
  }
}

void driving() {
  unsigned long currentTime = millis();

  /* 직진 및 우회전 (추후에 해당 로직을 이용하여 real_pos 계산) */
  switch (pathStep % 2) {
    case 0:
      if (currentTime - pathStartTime < STRIGHT_SEC) {
        forward();
      } else {
        pathStep++;
        pathStartTime = currentTime;
      }
      break;
    case 1:
      if (currentTime - pathStartTime < TURN_SEC) {
        right();
      } else {
        pathStep++;
        pathStartTime = currentTime;
      }
      break;
  }
}

void forward() {
  adjustMotorSpeed();
  motor1.run(FORWARD);
  motor2.run(FORWARD);
}

void right() {
  /* 모터의 스피드가 달라서.. 우회전 할 때는 경험적 값을 통해서 스피드를 설정합니다. */
  motor1.setSpeed(160);
  motor2.setSpeed(210);
  motor1.run(BACKWARD);
  motor2.run(FORWARD);
}


void sstop() {
  motor1.run(RELEASE);
  motor2.run(RELEASE);
}