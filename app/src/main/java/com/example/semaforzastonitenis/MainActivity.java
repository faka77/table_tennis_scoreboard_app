package com.example.semaforzastonitenis;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.felhr.usbserial.UsbSerialDevice;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private TextView textViewPlayer1, textViewPlayer2, textViewScore1, textViewScore2, textViewSet1, textViewSet2,
            textViewSet, textViewResetVrijeme;
    private Button buttonPlus1, buttonPlus2, buttonMinus1, buttonMinus2, buttonSwap, buttonReset, buttonStart, buttonConnect,
            buttonSetPlayer1Plus, buttonSetPlayer2Plus, buttonSetPlayer1Minus, buttonSetPlayer2Minus;
    private int scorePlayer1 = 0, scorePlayer2 = 0, setPlayer1 = 0, setPlayer2 = 0, currentSet = 0, swapCounter = 1;
    private UsbSerialDevice serialPort;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private final String DEVICE_ADDRESS = "98:D3:34:90:93:20";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    Runnable runnable;
    boolean timerRadi = false;
    long proslo = 0;

    private int seconds = 0;
    private boolean running;
    private boolean wasRunning;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        InitializeComponent();
        buttonPlus1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    domaciPlus();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);}}});
        buttonPlus2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    gostiPlus();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }}});
        buttonMinus1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scorePlayer1 > 0) {
                    domaciMinus();}}});
        buttonMinus2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scorePlayer2 > 0) {
                    gostiMinus();}}});
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonResetAll();}});
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonStartGame();
            }});
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SuperConnect();
                startGame();}});
        buttonSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swap();
            }});
        buttonSetPlayer1Plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData("22");
                setPlayer2++;
                currentSet++;
                if ((setPlayer2 == 3)) {
                    currentSet = 0;
                    scorePlayer2 = 0;
                    try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                    sendData("10");
                    resetAll();
                    textViewSet.setText(String.valueOf(currentSet));
                }
                updateSet();
            }
        });
        buttonSetPlayer2Plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlayer1++;
                currentSet++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                sendData("23");
                updateSet();
                if ((setPlayer1 == 3)) {
                    currentSet = 0;
                    scorePlayer2 = 0;
                   try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    sendData("9");
                    sendData("9");
                    resetAll();
                    textViewSet.setText(String.valueOf(currentSet));}}});
        buttonSetPlayer1Minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setPlayer2 > 0 && currentSet > 1) {
                    setPlayer2--;
                    currentSet--;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    sendData("24");
                    updateSet();
                    try {
                        updateScore();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }}}});

        buttonSetPlayer2Minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setPlayer1 > 0 && currentSet > 1) {
                    setPlayer1--;
                    currentSet--;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    sendData("25");
                    updateSet();
                }
            }
        });
    }
    @Override
    public void onSaveInstanceState(
            Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState
                .putInt("seconds", seconds);
        savedInstanceState
                .putBoolean("running", running);
        savedInstanceState
                .putBoolean("wasRunning", wasRunning);
    }
    private void InitializeComponent() {
        textViewPlayer1 = findViewById(R.id.textViewPlayer1);
        textViewPlayer2 = findViewById(R.id.textViewPlayer2);
        textViewScore1 = findViewById(R.id.textViewScore1);
        textViewScore2 = findViewById(R.id.textViewScore2);
        textViewSet1 = findViewById(R.id.textViewSet1);
        textViewSet2 = findViewById(R.id.textViewSet2);
        textViewSet = findViewById(R.id.textViewSet);
        buttonPlus1 = findViewById(R.id.buttonPlus1);
        buttonPlus2 = findViewById(R.id.buttonPlus2);
        buttonMinus1 = findViewById(R.id.buttonMinus1);
        buttonMinus2 = findViewById(R.id.buttonMinus2);
        buttonSwap = findViewById(R.id.buttonSwap);
        buttonReset = findViewById(R.id.buttonReset);
        buttonStart = findViewById(R.id.buttonStart);
        buttonConnect = findViewById(R.id.buttonConnect);
        buttonSetPlayer1Plus = findViewById(R.id.buttonSetPlayer1Plus);
        buttonSetPlayer1Minus = findViewById(R.id.buttonSetPlayer1Minus);
        buttonSetPlayer2Plus = findViewById(R.id.buttonSetPlayer2Plus);
        buttonSetPlayer2Minus = findViewById(R.id.buttonSetPlayer2Minus);
        buttonStart.setEnabled(false);
        buttonReset.setEnabled(false);
        buttonMinus1.setEnabled(false);
        buttonMinus2.setEnabled(false);
        buttonPlus1.setEnabled(false);
        buttonPlus2.setEnabled(false);
        buttonSwap.setEnabled(false);

        //resetAll();
    }

    private void buttonResetAll() {
        swapCounter = 1;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sendData("100");
        setPlayer1 = 0;
        setPlayer2 = 0;
        currentSet = 0;
        scorePlayer1 = 0;
        scorePlayer2 = 0;
        textViewScore1.setText(String.valueOf(scorePlayer1));
        textViewScore2.setText(String.valueOf(scorePlayer2));
        textViewSet.setText(String.valueOf(currentSet));
        textViewSet1.setText(String.valueOf(setPlayer1));
        textViewSet2.setText(String.valueOf(setPlayer2));
        buttonStart.setEnabled(true);
        buttonReset.setEnabled(false);
        buttonMinus1.setEnabled(false);
        buttonMinus2.setEnabled(false);
        buttonPlus1.setEnabled(false);
        buttonPlus2.setEnabled(false);
        buttonSwap.setEnabled(false);
        startGame();
    }


    private void buttonStartGame() {


    }

    boolean odigrano = false;

    private void domaciPlus() throws InterruptedException {
        sendData("1");
        scorePlayer1++;
        textViewScore1.setText(String.valueOf(scorePlayer1));

        if (currentSet > 4) {
            if (scorePlayer1 == 5 && scorePlayer1 > scorePlayer2) {
             Thread.sleep(1000);
                swap();
                sendData("16");
            }
        }
        if ((scorePlayer1 >= 10 && scorePlayer2 >= 10)) {
            odigrano = true;
            if (scorePlayer1 - scorePlayer2 == 2) {
                sendData("6");
                scorePlayer1 = 0;
                scorePlayer2 = 0;
                setPlayer1++;
                currentSet++;
                Thread.sleep(1000);
                sendData("7");
                if ((setPlayer1 == 3)) {
                    currentSet = 0;
                    scorePlayer1 = 0;
                    /*serialPort1.WriteLine("12");*/
                   Thread.sleep(1000);
                    sendData("9");
                    resetAll();
                /*
                button1.Enabled = false;
                button2.Enabled = false;
                button3.Enabled = false;
                button4.Enabled = false;
                */
                    textViewSet.setText(String.valueOf(currentSet));
                }
                else {
                       Thread.sleep(1000);
                    sendData("15");
                //writeline12
                textViewScore1.setText(String.valueOf(scorePlayer1));
                textViewScore2.setText(String.valueOf(scorePlayer2));
                textViewSet.setText(String.valueOf(currentSet));
                textViewSet1.setText(String.valueOf(setPlayer1));
                textViewSet2.setText(String.valueOf(setPlayer2));
                swap();
                odigrano = false;}
            }
        }


        if ((scorePlayer1 >= 11) && odigrano == false) {
            sendData("6");
            Thread.sleep(1000);
            sendData("7");
            scorePlayer1 = 0;
            scorePlayer2 = 0;
            setPlayer1++;
            currentSet++;
            Thread.sleep(500);
            sendData("15");
            /*serialPort1.WriteLine("12");*/
            textViewScore1.setText(String.valueOf(scorePlayer1));
            textViewScore2.setText(String.valueOf(scorePlayer2));
            textViewSet.setText(String.valueOf(currentSet));
            textViewSet1.setText(String.valueOf(setPlayer1));
            textViewSet2.setText(String.valueOf(setPlayer2));
            swap();


            if ((setPlayer1 == 3)) {
                currentSet = 0;
                scorePlayer2 = 0;
                /*serialPort1.WriteLine("12");*/
                Thread.sleep(1000);
                sendData("9");
                resetAll();
                /*
                button1.Enabled = false;
                button2.Enabled = false;
                button3.Enabled = false;
                button4.Enabled = false;
                */
                textViewSet.setText(String.valueOf(currentSet));
            }
        }
    }

    private void domaciMinus() {
        if (scorePlayer1 > 0) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            sendData("2");
            scorePlayer1--;
            textViewScore1.setText(String.valueOf(scorePlayer1));
        }
    }

    private void gostiPlus() throws InterruptedException {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sendData("3");
        scorePlayer2++;
        textViewScore2.setText(String.valueOf(scorePlayer2));

        if (currentSet > 4) {
            if (scorePlayer2 == 5 && scorePlayer2 > scorePlayer1) {
                swap();
                Thread.sleep(1000);
                sendData("16");
            }
        }
        if ((scorePlayer1 >= 10 && scorePlayer2 >= 10)) {
            odigrano = true;
            if (scorePlayer2 - scorePlayer1 == 2) {
                sendData("5");
                Thread.sleep(500);
                sendData("7");
                scorePlayer1 = 0;
                scorePlayer2 = 0;
                setPlayer2++;
                currentSet++;
                Thread.sleep(500);
                sendData("15");
                /*serialPort1.WriteLine("12");*/
                textViewScore1.setText(String.valueOf(scorePlayer1));
                textViewScore2.setText(String.valueOf(scorePlayer2));
                textViewSet.setText(String.valueOf(currentSet));
                textViewSet1.setText(String.valueOf(setPlayer1));
                textViewSet2.setText(String.valueOf(setPlayer2));
                swap();
                odigrano = false;
                if ((setPlayer2 == 3)) {
                    currentSet = 0;
                    scorePlayer2 = 0;
                    /*serialPort1.WriteLine("12");*/
                      try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    sendData("10");

                    resetAll();
                /*
                button1.Enabled = false;
                button2.Enabled = false;
                button3.Enabled = false;
                button4.Enabled = false;
                */
                    textViewSet.setText(String.valueOf(currentSet));
                }
            }
        }
        if ((scorePlayer2 >= 11) && odigrano == false) {
            sendData("5");
            Thread.sleep(1000);
            sendData("7");
            scorePlayer1 = 0;
            scorePlayer2 = 0;
            setPlayer2++;
            currentSet++;
            Thread.sleep(1000);
            sendData("15");


            /*serialPort1.WriteLine("12");*/
            textViewScore1.setText(String.valueOf(scorePlayer1));
            textViewScore2.setText(String.valueOf(scorePlayer2));
            textViewSet.setText(String.valueOf(currentSet));
            textViewSet1.setText(String.valueOf(setPlayer1));
            textViewSet2.setText(String.valueOf(setPlayer2));
            swap();
            if ((setPlayer2 == 3)) {
                currentSet = 0;
                scorePlayer2 = 0;
                /*serialPort1.WriteLine("12");*/
    try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                    throw new RuntimeException(e);
                }
                sendData("10");

                resetAll();
                /*
                button1.Enabled = false;
                button2.Enabled = false;
                button3.Enabled = false;
                button4.Enabled = false;
                */
                textViewSet.setText(String.valueOf(currentSet));
            }


        }
    }

    private void gostiMinus() {
        if (scorePlayer2 > 0) {
            sendData("4");
            scorePlayer2--;
            textViewScore2.setText(String.valueOf(scorePlayer2));
        }
    }

    private void updateScore() throws RuntimeException, InterruptedException {
        textViewScore1.setText(String.valueOf(scorePlayer1));
        textViewScore2.setText(String.valueOf(scorePlayer2));

        if (scorePlayer1 >= 11 && scorePlayer1 - scorePlayer2 >= 2) {
            Thread.sleep(500);
            textViewScore1.setText("11");
            sendData("5");
            Thread.sleep(500);
            sendData("7");
            Thread.sleep(500);
            sendData("15");
            scorePlayer1 = 0;
            scorePlayer2 = 0;
            setPlayer1++;
            currentSet++;
            resetScore();
            if ((setPlayer1 == 3)) {
                currentSet = 0;
                scorePlayer1 = 0;
                /*serialPort1.WriteLine("12");*/
              Thread.sleep(1000);
                sendData("9");
                resetAll();
                /*
                button1.Enabled = false;
                button2.Enabled = false;
                button3.Enabled = false;
                button4.Enabled = false;
                */
                textViewSet.setText(String.valueOf(currentSet));
            }
            swap();

        } else if (scorePlayer2 >= 11 && scorePlayer2 - scorePlayer1 >= 2) {
            textViewScore2.setText("11");
            Thread.sleep(500);
            sendData("6");
            Thread.sleep(500);
            sendData("7");
            Thread.sleep(500);
            sendData("15");
            //sendData("21"); isti kurac
            setPlayer2++;
            currentSet++;
            resetScore();
            swap();
            if ((setPlayer2 == 3)) {
                currentSet = 0;
                scorePlayer2 = 0;
                /*serialPort1.WriteLine("12");*/
                Thread.sleep(500);
                sendData("10");
                resetAll();
                /*
                button1.Enabled = false;
                button2.Enabled = false;
                button3.Enabled = false;
                button4.Enabled = false;
                */
                textViewSet.setText(String.valueOf(currentSet));
            }
        }
        if (currentSet == 5 && scorePlayer1 > scorePlayer2 && scorePlayer1 == 5) {
            swap();
            Thread.sleep(500);
            sendData("16");
        } else if (currentSet == 5 && scorePlayer2 > scorePlayer1 && scorePlayer2 == 5) {
            swap();
            Thread.sleep(500);
            sendData("16");
        }


        textViewSet1.setText(String.valueOf(setPlayer1));
        textViewSet2.setText(String.valueOf(setPlayer2));
        textViewSet.setText(String.valueOf(currentSet));

        if (setPlayer1 == 3) {
              try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            sendData("9");
            resetAll();
        } else if (setPlayer2 == 3) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            sendData("10");

            resetAll();
        }
    }

    private void updateSet() {
        textViewSet1.setText(String.valueOf(setPlayer1));
        textViewSet2.setText(String.valueOf(setPlayer2));
        textViewSet.setText(String.valueOf(currentSet));
    }

    private void resetScore() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sendData("7");
        scorePlayer1 = 0;
        scorePlayer2 = 0;
        textViewScore1.setText("0");
        textViewScore2.setText("0");

    }

    private void resetAll() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sendData("100");
        swapCounter = 1;
        scorePlayer1 = 0;
        scorePlayer2 = 0;
        setPlayer1 = 0;
        setPlayer2 = 0;
        currentSet = 0;
        textViewScore1.setText("0");
        textViewScore2.setText("0");
        textViewSet.setText("0");
        textViewSet1.setText("0");
        textViewSet2.setText("0");
        startGame();
    }

    private void startGame() {
        try {
            Thread.sleep(1000
            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sendData("101");
        currentSet = 1;
        scorePlayer1 = 0;
        scorePlayer2 = 0;
        setPlayer1 = 0;
        setPlayer2 = 0;
        textViewSet.setText(String.valueOf(currentSet));
        textViewScore1.setText(String.valueOf(scorePlayer1));
        textViewScore2.setText(String.valueOf(scorePlayer2));
        buttonReset.setEnabled(true);
        buttonMinus1.setEnabled(true);
        buttonMinus2.setEnabled(true);
        buttonPlus1.setEnabled(true);
        buttonPlus2.setEnabled(true);
        buttonSwap.setEnabled(true);
    }

    private void SuperConnect() {
        resetAll();
        startGame();
        /*
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        bluetoothDevice=bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(PORT_UUID);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            makeText(this, "Connected?", LENGTH_SHORT).show();
            sendData("100");
        } catch (IOException e) {
            //e.printStackTrace();
            makeText(this, "Fail?", LENGTH_SHORT).show();
        }

         */
        try {
            resetAll();
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            bluetoothDevice = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);

            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(PORT_UUID);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            makeText(this, "Connected", LENGTH_SHORT).show();
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            sendData("100");
            buttonStart.setEnabled(true);
        } catch (IOException e) {
            e.printStackTrace();
            makeText(this, "Failed to connect", LENGTH_SHORT).show();
        }
        buttonStart.setEnabled(true);
    }



    private void sendData(String data) throws RuntimeException {
        if (outputStream != null) {
            try {
                outputStream.write(data.getBytes());
                outputStream.flush();
                Thread.sleep(1000);
                Log.d("SendData", "Data sent: " + data);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


        }
    }
    private void swap() throws RuntimeException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(swapCounter%2!=0){
            sendData("12");
            swapCounter++;
        }
        else if(swapCounter%2==0){
            sendData("13");
            swapCounter++;
        }
    }

}