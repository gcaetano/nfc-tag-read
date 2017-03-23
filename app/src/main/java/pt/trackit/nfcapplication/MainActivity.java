package pt.trackit.nfcapplication;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;

public class MainActivity extends Activity {

    private static final String TAG = "NfcDemo";

    private static byte OPTION_FLAG = 0x40;
    private static byte ADDRESS_FLAG = 0x20;
    private static byte SELECT_FLAG = 0x10;
    private static byte CMD_SYSTEM_INFORMATION = 0x2B;
    private static byte CMD_READ_SINGLE_BLOCK = 0x20;

    NfcAdapter nfcAdapter;
//    TextView promt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        promt = (TextView) findViewById(R.id.promt);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Log.i(TAG, "device doesn't support NFC！");
            finish();
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            Log.i(TAG, "please activate NFC！");
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }


    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            //System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    private void processIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String metaInfo = "";
        if(tag != null){
            int size = 0;
            NfcV tech = NfcV.get(tag);
//            for (String t : tag.getTechList()) {
//                System.out.println(t);
//                metaInfo += "Type:" + t + "\n";
//            }
            if (tech != null) {
                try {
                    tech.connect();
                    size = get15693Size(tech);
                    if (size > 0){
                        metaInfo = bytesToHexString(tag.getId());
                        metaInfo = unHex(metaInfo.substring(2));
//                        for(byte i=0; i<size; i++){
//                            byte[] block = readSingleBlock(i, tech);
//                            if(block != null) {
//                                metaInfo += "Block " + i + " : "
//                                        + bytesToHexString(block) + "\n";
//                            }
//                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        tech.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        Log.i(TAG, metaInfo);
    }

//    byte[] readSingleBlock(byte no, NfcV tech){
//        if(tech != null && tech.isConnected()){
//            Tag tag = tech.getTag();
//            byte[] id = tag.getId();
//            byte[] readCmd = new byte[3 + id.length];
//            readCmd[0] = ADDRESS_FLAG; // set "address" flag (only send command to this tag)
//            readCmd[1] = CMD_READ_SINGLE_BLOCK; // ISO 15693 Single Block Read command byte
//            System.arraycopy(id, 0, readCmd, 2, id.length); // copy ID
//            readCmd[2 + id.length] = no; // 1 byte payload: block address
//            try {
//                byte[] data = tech.transceive(readCmd);
//                Log.d(TAG, "block:"+no+"read:"+bytesToHexString(data));
//                return data;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }

    int get15693Size(NfcV tech){
        int size = -1;
        if(tech != null && tech.isConnected()){
            byte[] readCmd = new byte[2];
            readCmd[0] = 0x00;
            readCmd[1] = CMD_SYSTEM_INFORMATION;
            try {
                byte data[] = tech.transceive(readCmd);
                Log.d(TAG, "read:" + bytesToHexString(data));

                size = (int)data[data.length - 3];
                return size;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return size;
    }

    public static String unHex(String arg) {
        BigInteger bigInt = new BigInteger(arg, 16);
        return bigInt.toString();
    }
}
