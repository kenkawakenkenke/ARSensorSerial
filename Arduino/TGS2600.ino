void setup() {
  Serial.begin(9600);
}

void loop() {
//  if (Serial.read() == -1) {
//    delay(100);
//    return;
//  }
  int v=analogRead(0);
//  int capped = map(v, 0, 1024, 0, 256);


//  Serial.write(capped);

  int high = B10000000 | (v >> 5);
  int low =  (v & B11111);
  
//Serial.print(v, BIN);
////Serial.println();
//Serial.print(" ");
//  Serial.print(high, BIN);
//  Serial.print(" ");
//  Serial.print(low, BIN);
//  Serial.println();
  
//Serial.println(v);
  Serial.write(high);
  Serial.write(low);
  
//  if (v>255) {
//    v=255;
//  }
//  if (v<0) v=0;

char buf[50];
//sprintf(buf, "%d %d\n", highByte(v), lowByte(v));
//Serial.println("=====");
//Serial.println(buf);
//  Serial.println(v);

//Serial.write(highByte(v));
//Serial.write(lowByte(v));

//  Serial.write(v&0xff);
//  Serial.write(v);
//  Serial.println(v);
Serial.flush();
//  Serial.write((v>>8)&0xff);
//  Serial.write(v&0xff);
//  Serial.flush();
  delay(250);
}
