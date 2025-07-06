/* 
실험 시작 버튼 누르면 비콘 데이터 전송
실험 종료 버튼 누르면  자동차거리 데이터 전송
*/
#define LEFT_IR_PIN A0 //밝으면 숫자가 작은 상태
#define RIGHT_IR_PIN A5

//모터 속도를 변경하면 속력도 변경해야함.
#define MOTER1_SPEED 135 // PORT 4 (3:2 비율)
#define MOTER2_SPEED (MOTER1_SPEED * 2/3) // PORT 1

//감지값이 서로 다름
#define IRVALUE_RIGHT 1000 
#define IRVALUE_LEFT 400
#define CYCLEDISTANCE 760  // 1바퀴 이동 거리 (cm)

#include <AFMotor.h>
#include <SoftwareSerial.h>

SoftwareSerial mySerial(9, 10); // 블루투스 RX, TX

AF_DCMotor motor1(4);
AF_DCMotor motor2(1);

String ms = "";
bool engine_flag = false;
String state = "forward";

unsigned long startTime = 0;
unsigned long elapsedTime = 0;
unsigned long lastSendTime = 0;
float carSpeed = 33.0 / CYCLEDISTANCE; //hardcoding

unsigned long whiteStartTime = 0;
unsigned long blackStartTime = 0;

void setup() {
  mySerial.begin(9600); //블루투스 장치와 통신 
  Serial.begin(9600); //USB와 아두이노 간 통신
}

void loop() {
  // 블루투스 기기로 부터 정보 받기
  if (mySerial.available()) {
    ms = mySerial.readStringUntil('c');
    ms.trim();

    if (ms == "start") {
      engine_flag = true;
      state = "forward";
      startTime = millis(); //시작 시간 측정
      Serial.println("출발~");
    } else if (ms == "stop") {
      elapsedTime = millis() - startTime; // 이동시간 측정
      float distance = carSpeed * elapsedTime; //거리 측정

      mySerial.print("이동거리 : ");
      mySerial.print(distance);
      mySerial.println("cm");

      Serial.println(distance);
      Serial.println("멈춰~"); //arduino IDE 상
      engine_flag = false;
      elapsedTime = 0;
    }
    ms = "";
  }

  if (engine_flag) {
    driving();
  } else {
    sstop();
  }
}
/* 값이 작으면 검정 -> detect : 1
   값이 크면 흰색   -> detect : 0
*/

void driving() {
  int leftIR = analogRead(LEFT_IR_PIN);
  int rightIR = analogRead(RIGHT_IR_PIN);
  bool leftDetect = (leftIR > IRVALUE_LEFT);   // 검정 감지
  bool rightDetect = (rightIR > IRVALUE_RIGHT); // 검정 감지

  unsigned long now = millis();

  if (state == "forward") {
    // 양쪽 다 검정 = 중심, 직진
    if (leftDetect && rightDetect) {
      forward();
      whiteStartTime = 0;
    }
    // 왼쪽만 검정 = 오른쪽 치우침 → 왼쪽 회전
    else if (leftDetect && !rightDetect) {
      turnLeftSlight();
      whiteStartTime = 0;
    }
    // 오른쪽만 검정 = 왼쪽 치우침 → 오른쪽 회전
    else if (!leftDetect && rightDetect) {
      turnRightSlight();
      whiteStartTime = 0;
    }
    // 양쪽 다 흰색 = 선 이탈 → 코너일 가능성
    else {
      if (whiteStartTime == 0) whiteStartTime = now;

      if (now - whiteStartTime > 200) {
        state = "turning";
        whiteStartTime = 0;
      }
    }
  }

  else if (state == "turning") {
    right();  // 오른쪽 회전

    // 회전 중에 다시 검정 감지되면 멈추고 직진 상태로
    if (leftDetect || rightDetect) {
      if (blackStartTime == 0) blackStartTime = now;

      if (now - blackStartTime > 100) {
        state = "forward";
        blackStartTime = 0;
      }
    } else {
      blackStartTime = 0;
    }
  }
  Serial.print("  leftDetect: "); Serial.print(leftDetect);
  Serial.print("  rightDetect: "); Serial.print(rightDetect);
  Serial.print("  leftValue: "); Serial.print(leftIR);
  Serial.print("  rightValue: "); Serial.print(rightIR);
  Serial.print("  State: "); Serial.println(state);
  delay(100);
}

void forward() {
  motor1.setSpeed(MOTER1_SPEED);
  motor2.setSpeed(MOTER2_SPEED);
  motor1.run(FORWARD);
  motor2.run(FORWARD);
}

void turnLeftSlight() {
  motor1.setSpeed(MOTER1_SPEED* 2/3);  // 왼쪽 느리게
  motor2.setSpeed(30);
  motor1.run(FORWARD);
  motor2.run(FORWARD);
}

void turnRightSlight() {
  motor1.setSpeed(10);
  motor2.setSpeed(MOTER2_SPEED); 
  motor1.run(FORWARD);
  motor2.run(FORWARD);
}

void right() {
  motor1.setSpeed(0);
  motor2.setSpeed(MOTER2_SPEED*1/2);
  motor1.run(BACKWARD);
  motor2.run(FORWARD);
}

void sstop() {
  motor1.run(RELEASE);
  motor2.run(RELEASE);
}

