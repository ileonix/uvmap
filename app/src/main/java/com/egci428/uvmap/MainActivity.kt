package com.egci428.uvmap

import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import io.netpie.microgear.Microgear
import io.netpie.microgear.MicrogearEventListener
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val microgear = Microgear(this)
    private val appid: String = "uvmaps" //val is const var is normal
    private val key: String = "U5R4jJnZAl7ZU0J"
    private val secret: String = "IQcWCYWf3M7IHDuebPut6yrK1"
    private val alias: String = "android"
    var msghand:String = ""
    // Write a message to the database
    //var database: DatabaseReference = FirebaseDatabase.getInstance().getReference() //    DatabaseReference database = FirebaseDatabase.getInstance().getReference()
    // var myRef: DatabaseReference = database.child("logUV") //    DatabaseReference myRef = database.child("logUV/")
    // Read from the database


    var handler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            val bundle = msg.data //get data and put msg to textview via function in class microgearcallback
            val string = bundle.getString("myKey") //recieve from onMessage
            //val myTextView = findViewById<TextView>(R.id.DatatextView)
            //myTextView.append(string+"\n"); //สำหรับการเว้นบรรทัดไปเรื่อยๆ
            //DatatextView.text = string
            if(string != null){
                var parts = string.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var p1 = parts[0] //UV
                var p2 = parts[1] //temperature
                var p3 = parts[2] //humid
                var p4 = parts[3] //velocity
                var p5 = parts[4] //lat
                var p6 = parts[5] //long
                var p7 = parts[6] //time

                uvtextView.text = p1
                if(p1.toFloat() >=0 && p1.toFloat() <=2){
                    uvtextView.setTextColor(Color.parseColor("#1CBA15"))
                }else if(p1.toFloat()>2 && p1.toFloat()<=5){
                    uvtextView.setTextColor(Color.parseColor("#F2EF09"))
                }else if(p1.toFloat()>5 && p1.toFloat()<=7){
                    uvtextView.setTextColor(Color.parseColor("#FF8F00"))
                }else if(p1.toFloat()>7 && p1.toFloat()<=10){
                    uvtextView.setTextColor(Color.parseColor("#FF0800"))
                }else if(p1.toFloat()>=11){
                    uvtextView.setTextColor(Color.parseColor("#7600BF"))
                }
                timetextView.text = p7
                temptextView.text = "T: "+p2+" °C"
                temptextView.setTextColor(Color.parseColor("#FF4933"))
                humidtextView.text = "H: "+p3+" %"
                humidtextView.setTextColor(Color.parseColor("#33C1FF"))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val callback = MicrogearCallBack()
        microgear.connect(appid, key, secret, alias)
        microgear.setCallback(callback)
        microgear.subscribe("uvmaps_m1") //get data from sensor

//        mapBtn.setOnClickListener {
//            var intent = Intent(this, mapsActivity::class.java)
//            //intent.putExtra("logincheck",false)
//            startActivity(intent)
//        }




        //firebasetextView.text = database.child("-KubrnZM0SKJ2Ri9PHvl").child("UVIndex").orderByValue().toString()

//        var uvQuery: Query = myRef.orderByKey()
//        uvQuery.addListenerForSingleValueEvent( ValueEventListener(){
//            fun onDataChange(dataSnapshot: DataSnapshot) {
//                for(singleSnapshot: DataSnapshot in dataSnapshot.children){
//                    var user = singleSnapshot.getValue(true)
//                }
//            }
//            fun onCancelled(databaseError: DatabaseError) {
//                Log.e("onCancelled", databaseError.toException().toString())
//            }
//        })
        //Log.d("firebase",myRef.toString())
    }
    override fun onDestroy() {
        super.onDestroy()
        microgear.disconnect()
    }
    override fun onResume() {
        super.onResume()
        microgear.bindServiceResume()
    }

    internal inner class MicrogearCallBack : MicrogearEventListener {
        override fun onConnect() {
            val msg = handler.obtainMessage()
            val bundle = Bundle()
            //bundle.putString("myKey", "Now I'm connected with netpie\n")
            msg.data = bundle
            handler.sendMessage(msg)
            Log.i("Connected", "Now I'm connected with netpie")
        }

        @SuppressLint("NewApi")
        override fun onMessage(topic: String, message: String) {
            val msg = handler.obtainMessage()
            val bundle = Bundle()
            //bundle.putString("myKey", topic+" : "+message);
            bundle.putString("myKey", message) //config to show just data that nodemcu microgear publish
            //split bundle
            val newMsg = message
            //String state = "";
            val parts = newMsg.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val p1 = parts[0] //NowString()
            val p2 = parts[1] //latitude
            val p3 = parts[2] //longitude
            val p4 = parts[3] //velocity
            val p5 = parts[4] //uv
            val p6 = parts[5] //temperature
            //if(Objects.equals(p6, "1")){ state = "ON"; }
            //else if(Objects.equals(p6, "0")){ state = "OFF"; }
            //if(p6 != "0" || p6 != "1"){ microgear.chat("plantone","OFF"); state = "MAN";}
            val p7 = parts[6] //strh
            //String p8 = parts[7]; //uptime
//            bundle.putString("myKey", "UV index " + p5 + "" + "\nอุณหภูมิในอากาศ " + p6 + " °C" + "\nความชื้นในอากาศ " + p7 + " %"
//                    + "\nความเร็ว " + p4 + " Km/h" + "\n|Latitude" + p2 + ":" + "\tLongitude" + p3 + "|"
//                    + "\n|TIME: " + p1 + "|")
            bundle.putString("myKey",p5+","+p6+","+p7+","+p4+","+p2+","+p3+","+p1)
            msg.data = bundle
            handler.sendMessage(msg)
            Log.i("Message", topic + " : " + message)
        }

        override fun onPresent(token: String) {
            val msg = handler.obtainMessage()
            val bundle = Bundle()
            //bundle.putString("myKey", "New friend Connect :" + token + "\n")
            msg.data = bundle
            //handler.sendMessage(msg);
            Log.i("present", "New friend Connect :" + token)
        }

        override fun onAbsent(token: String) {
            val msg = handler.obtainMessage()
            val bundle = Bundle()
            //bundle.putString("myKey", "Friend lost :" + token + "\n")
            msg.data = bundle
            //handler.sendMessage(msg);
            Log.i("absent", "Friend lost :" + token)
        }

        override fun onDisconnect() {
            val msg = handler.obtainMessage()
            val bundle = Bundle()
            //bundle.putString("myKey", "Disconnected" + "\n")
            msg.data = bundle
            //handler.sendMessage(msg);
            Log.i("disconnect", "Disconnected")
        }

        override fun onError(error: String) {
            val msg = handler.obtainMessage()
            val bundle = Bundle()
            //bundle.putString("myKey", "Exception : " + error + "\n")
            msg.data = bundle
            //handler.sendMessage(msg);
            Log.i("exception", "Exception : " + error)
        }

        override fun onInfo(info: String) {
            val msg = handler.obtainMessage()
            val bundle = Bundle()
            //bundle.putString("myKey", "Exception : " + info + "\n")
            msg.data = bundle
            //handler.sendMessage(msg);
            Log.i("info", "Info : " + info)
        }
    }
}
