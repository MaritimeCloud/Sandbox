import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;

public class ECDSAExample {

    public static void main(String[] args) throws Exception {
        /*
         * Generate an ECDSA signature
         */

        /*
         * Generate a key pair
         */

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        SecureRandom random = SecureRandom.getInstanceStrong();

        keyGen.initialize(256, random);

        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey priv = pair.getPrivate();
        PublicKey pub = pair.getPublic();

        /*
         * Create a Signature object and initialize it with the private key
         */

        Signature dsa = Signature.getInstance("SHA256withECDSA");

        for (Provider p : Security.getProviders()) {
            System.out.println(p.getName());
            for (Service s : p.getServices()) {
                System.out.println("  " + s.getAlgorithm());
            }
        }
        dsa.initSign(priv);

        String str = "This is string to sign";
        byte[] strByte = str.getBytes("UTF-8");
        dsa.update(strByte);

        /*
         * Now that all the data to be signed has been read in, generate a signature for it
         */

        byte[] realSig = dsa.sign();

        System.out.println(realSig.length);
        System.out.println("Signature: " + new BigInteger(1, realSig).toString(16));

    }
}
// 304502204ba07316db76db76703b4dac3eea5b5e91e31d5912aa6d90a2fcf456d51ed305022100eb93ddb6487c5c12de60e7cca6571eb1807f4178386364ea32ce55cfb2b48bb5
// 304502206958d98c3a13b1aca0f91646168855c2f3b37b139cddad3f5c346f110e70cdc0022100f0f93669afdbe0f0bff973952e5aba666d9d1d9a438b9befd2a001e6bf80640d
