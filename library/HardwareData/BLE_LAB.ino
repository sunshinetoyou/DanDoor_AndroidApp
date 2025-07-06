#define LEFT_IR_PIN A0
#define RIGHT_IR_PIN A1

#define MOTER1_SPEED 135 // PORT 4 
#define MOTER2_SPEED 90  // PORT 1

#define IRVALUE 400
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
float carSpeed = 33.0 / CYCLEDISTANCE;

int turningCount = 0;
unsigned long whiteStartTime = 0;
unsigned long blackStartTime = 0;

void setup() {
  mySerial.begin(9600);
  Serial.begin(9600);
}

void loop() {
  if (mySerial.available()) {
    ms = mySerial.readStringUntil('c');
    ms.trim();

    if (ms == "start") {
      engine_flag = true;
      state = "forward";
      startTime = millis();
      lastSendTime = millis();
      turningCount = 0;
    } else if (ms == "stop") {
      engine_flag = false;
      elapsedTime = 0;
    }
    ms = "";
  }

  if (engine_flag) {
    driving();

    unsigned long now = millis();
    if (now - lastSendTime >= 3000) {
      elapsedTime = (now - startTime) / 1000;
      float distance = carSpeed * elapsedTime;

      mySerial.print("이동 거리: ");
      mySerial.print(distance);
      mySerial.println(" cm");

      lastSendTime = now;
    }
  } else {
    sstop();
  }
}

void driving() {
  int leftIR = analogRead(LEFT_IR_PIN);
  int rightIR = analogRead(RIGHT_IR_PIN);
  bool leftDetect = (leftIR < IRVALUE);   // 검정 감지
  bool rightDetect = (rightIR < IRVALUE);

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

      if (now - whiteStartTime > 500) {
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
        turningCount++;

        Serial.print("회전 횟수: ");
        Serial.println(turningCount);

        if (turningCount % 4 == 0 && elapsedTime > 0) {
          carSpeed = (float)CYCLEDISTANCE / elapsedTime;
          startTime = millis();
        }
      }
    } else {
      blackStartTime = 0;
    }
  }
}

void forward() {
  motor1.setSpeed(MOTER1_SPEED);
  motor2.setSpeed(MOTER2_SPEED);
  motor1.run(FORWARD);
  motor2.run(FORWARD);
}

void turnLeftSlight() {
  motor1.setSpeed(60);  // 왼쪽 느리게
  motor2.setSpeed(MOTER2_SPEED);
  motor1.run(FORWARD);
  motor2.run(FORWARD);
}

void turnRightSlight() {
  motor1.setSpeed(MOTER1_SPEED);
  motor2.setSpeed(60);  // 오른쪽 느리게
  motor1.run(FORWARD);
  motor2.run(FORWARD);
}

void right() {
  motor1.setSpeed(0);
  motor2.setSpeed(MOTER2_SPEED);
  motor1.run(BACKWARD);
  motor2.run(FORWARD);
}

void sstop() {
  motor1.run(RELEASE);
  motor2.run(RELEASE);
}
