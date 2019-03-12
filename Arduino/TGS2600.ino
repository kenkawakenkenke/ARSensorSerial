void setup() {
  // 9600bpsでシリアル通信を開始
  Serial.begin(9600);
}

void loop() {
  // センサはA0から入力される。値は0~1024。
  int v=analogRead(0);
  // 上位バイト：最上位ビットにフラグを立て、上位5ビットを下位ビットに置く。
  Serial.write(B10000000 | (v >> 5));
  // 下位バイト：下位5ビットだけをそのまま送る。
  Serial.write(v & B11111);
  // 送信が完了するまで待つ。
  Serial.flush();
  delay(100);
}

