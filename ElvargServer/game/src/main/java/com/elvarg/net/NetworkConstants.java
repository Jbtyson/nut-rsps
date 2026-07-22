package com.elvarg.net;

import java.math.BigInteger;

import io.netty.util.AttributeKey;


/**
 * A class containing different attributes
 * which affect networking in different ways.
 *
 * @author Swiffy
 */
public class NetworkConstants {

    /**
     * The game port
     */
    public static final int GAME_PORT = 43595;

    /**
     * The opcode for requesting a login.
     */
    public static final int LOGIN_REQUEST_OPCODE = 14;

    /**
     * Signifies a new connection.
     */
    public static final int NEW_CONNECTION_OPCODE = 16;

    /**
     * Signifies the return of an existing connection.
     */
    public static final int RECONNECTION_OPCODE = 18;

    /**
     * The time required for the channel to time out
     */
    public static final int SESSION_TIMEOUT = 15;

    /**
     * The keys used for encryption on login
     */
    /**
     * The RSA keys used to decrypt the login block.
     *
     * <p>The modulus is public and may be shipped in the client. The private
     * exponent is a secret and should be supplied per-deployment via the
     * {@code ELVARG_RSA_PRIVATE_EXPONENT} (and optionally
     * {@code ELVARG_RSA_MODULUS}) environment variables. The bundled defaults
     * are a freshly generated 2048-bit development keypair — they replace the
     * publicly-known stock Elvarg key, but production servers should override
     * them with their own secret key (and embed the matching modulus in the
     * client's {@code Buffer.RSA_MODULUS}).
     */
    private static final BigInteger DEFAULT_RSA_MODULUS = new BigInteger("20794267407987247165754620164665110058879932647267446177268357275109906564048457021173613253874865366196503485119734410993749506432054797077815731653232588045152110071131986342187697401995552799086877445878163191970703480662497388725422913368908741012564295278069469245846249232450584075971334632282337815007919991989288300455152784334542323972180199306983771098647496854399624233948322950657216293343141510590389268518428157198089410238203179230281736940019341311522687858713798251368357138090410353190675648702396659073202311709738275598540368790281077972582279375677606966220591316433166751789131192079488592580399");
    private static final BigInteger DEFAULT_RSA_PRIVATE_EXPONENT = new BigInteger("7256750993913000746570241509163613411609364779829008979970163407526110029851110983585786788064330771782050310636334371634924469560820990645997277852832484263556066183024240652336463788105044296625051101891747720578634348631032512588906227805061443393783023289206506723251744300095782663251909522482404567908807797213534275085781487481714434510792685070284427750812039130981719627394796722151472775365087663604495277415010176943096649821981993685378863581705916439868085483177124943621744338056325630233989144877889007071097800297913759394766703468716356334552956426413489399088613967323035984162322349704997613787161");

    public static final BigInteger RSA_MODULUS = readBigInteger("ELVARG_RSA_MODULUS", DEFAULT_RSA_MODULUS);

    public static final BigInteger RSA_EXPONENT = readBigInteger("ELVARG_RSA_PRIVATE_EXPONENT", DEFAULT_RSA_PRIVATE_EXPONENT);

    private static BigInteger readBigInteger(String envVar, BigInteger fallback) {
        String value = System.getenv(envVar);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return new BigInteger(value.trim());
            } catch (NumberFormatException e) {
                System.err.println("Invalid value for " + envVar + "; using bundled default key.");
            }
        }
        return fallback;
    }

    /**
     * The amount of connections that are allowed from the same host.
     */
    public static final int CONNECTION_LIMIT = 2;

    /**
     * The attribute that contains the key for a players session.
     */
    public static final AttributeKey<PlayerSession> SESSION_KEY = AttributeKey.valueOf("session.key");

    /**
     * The maximum amount of messages that can be processed in one sequence.
     */
    public static final int PACKET_PROCESS_LIMIT = 30;


}
