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
     * are a freshly generated 1024-bit development keypair — they replace the
     * publicly-known stock Elvarg key, but production servers should override
     * them with their own secret key (and embed the matching modulus in the
     * client's {@code Buffer.RSA_MODULUS}).
     *
     * <p>NOTE: the RS #317 login protocol encodes the login-block size in a
     * single byte, so the RSA block must stay under ~255 bytes — i.e. the key
     * must be 1024-bit. A larger key requires widening the size fields in both
     * the client login writer and {@link LoginDecoder}.
     */
    private static final BigInteger DEFAULT_RSA_MODULUS = new BigInteger("129273559530516278128864119602682781249885808545146039511919136176498077884270822062656936510937470337139723277408748701376423798865456732215074171237047336990618336212826061734147003577520514866596121876291628816060074514857025152170225673571076569196785476667537365243160507929945059847455126170760800010141");
    private static final BigInteger DEFAULT_RSA_PRIVATE_EXPONENT = new BigInteger("96644003375156706088525528173908522931140503679897313058066101240025074781266291208933527692042378491053309458422595520453146833474419832993233730865909839538130552172022336983685598872477310535143176341242927103679116573616903778294370192126030180540878532574555551313778030469179050499790078822054999929813");

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
