/* The following code was generated by JFlex 1.5.1 */

package org.jetbrains.yesod.hamlet.parser;

/**
 * @author Leyla H
 */

import java.util.*;
import com.intellij.lexer.*;
import com.intellij.psi.*;
import org.jetbrains.haskell.parser.token.*;
import com.intellij.psi.tree.IElementType;


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.5.1
 * from the specification file <tt>/Users/atsky/work/haskell-idea-plugin/plugin/src/org/jetbrains/yesod/hamlet/parser/Hamlet.flex</tt>
 */
class _HamletLexer implements FlexLexer {
  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0, 0
  };

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED = 
    "\11\0\1\2\1\4\1\45\1\46\1\45\22\0\1\2\1\15\1\44"+
    "\1\17\1\5\5\0\1\34\2\0\1\3\1\42\1\47\12\1\1\43"+
    "\1\0\1\16\1\51\1\52\1\40\1\37\32\1\1\0\1\41\1\0"+
    "\1\34\1\36\1\0\1\25\1\27\1\10\1\6\1\14\1\21\1\32"+
    "\1\31\1\20\2\1\1\22\1\26\1\30\1\7\1\13\1\1\1\24"+
    "\1\23\1\11\2\1\1\33\1\1\1\12\1\1\1\35\1\0\1\50"+
    "\7\0\1\45\u1fa2\0\1\45\1\45\udfd6\0";

  /** 
   * Translates characters to character classes
   */
  private static final char [] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\1\0\1\1\1\2\1\3\1\2\1\4\2\1\1\5"+
    "\1\6\1\1\1\2\1\1\1\6\1\7\2\1\1\10"+
    "\1\11\1\12\1\13\1\2\4\0\1\14\5\0\1\15"+
    "\1\16\1\17\1\20\1\0\1\21\3\0\1\22\1\23"+
    "\1\0\1\24\6\0\1\16\1\0\1\25\7\0\1\26"+
    "\1\0\1\24\7\0";

  private static int [] zzUnpackAction() {
    int [] result = new int[71];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\53\0\126\0\201\0\254\0\53\0\327\0\u0102"+
    "\0\u012d\0\u0158\0\u0183\0\u01ae\0\u01d9\0\53\0\u0204\0\u022f"+
    "\0\u025a\0\53\0\53\0\53\0\53\0\u0285\0\u02b0\0\u02db"+
    "\0\u0306\0\u0331\0\53\0\u035c\0\u0387\0\u03b2\0\u03dd\0\u0408"+
    "\0\53\0\u0433\0\u045e\0\53\0\u0183\0\u0204\0\u0489\0\u025a"+
    "\0\u04b4\0\53\0\53\0\u04df\0\53\0\u050a\0\u0535\0\u0560"+
    "\0\u058b\0\u05b6\0\u05e1\0\53\0\u060c\0\53\0\u0637\0\u0662"+
    "\0\u068d\0\u06b8\0\u06e3\0\u070e\0\u0739\0\u0764\0\u078f\0\u07ba"+
    "\0\u07e5\0\u0810\0\u083b\0\u0866\0\u0891\0\u08bc\0\u08e7";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[71];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\2\1\3\1\4\1\5\1\6\1\7\7\3\1\10"+
    "\1\11\1\12\14\3\1\13\1\2\1\14\1\15\1\2"+
    "\1\16\1\17\1\20\1\21\1\0\1\4\1\22\1\23"+
    "\1\24\1\25\54\0\1\3\1\0\1\3\2\0\7\3"+
    "\3\0\14\3\2\0\1\3\16\0\1\4\43\0\1\4"+
    "\5\0\1\3\1\0\1\26\2\0\7\3\3\0\14\3"+
    "\2\0\1\3\22\0\1\27\1\30\1\31\3\0\1\32"+
    "\2\0\1\33\1\30\1\34\4\0\1\35\1\0\1\36"+
    "\2\0\1\37\34\0\1\40\40\0\1\41\11\0\1\42"+
    "\36\0\1\43\1\0\1\43\2\0\7\43\3\0\14\43"+
    "\1\0\1\44\1\43\51\0\1\44\16\0\1\3\1\0"+
    "\1\3\2\0\7\3\3\0\14\3\1\0\1\44\1\3"+
    "\51\0\1\44\2\0\1\45\13\0\1\46\1\0\1\46"+
    "\2\0\7\46\3\0\14\46\2\0\1\46\15\0\1\47"+
    "\1\0\1\47\2\0\7\47\3\0\14\47\2\0\1\47"+
    "\14\0\4\50\1\0\34\50\1\51\2\50\1\52\6\50"+
    "\1\0\1\3\1\0\1\3\2\0\7\3\3\0\14\3"+
    "\2\0\1\3\13\0\1\53\7\0\1\54\64\0\1\55"+
    "\56\0\1\56\47\0\1\57\37\0\1\60\70\0\1\61"+
    "\34\0\1\62\63\0\1\63\47\0\1\64\40\0\1\65"+
    "\50\0\1\43\1\0\1\43\2\0\7\43\3\0\14\43"+
    "\2\0\1\43\15\0\1\47\1\0\1\47\2\0\7\47"+
    "\3\0\14\47\2\0\1\47\4\0\1\66\7\0\4\50"+
    "\1\0\40\50\2\0\4\50\10\0\1\67\65\0\1\70"+
    "\52\0\1\71\53\0\1\72\40\0\1\73\51\0\1\74"+
    "\52\0\1\75\44\0\1\76\60\0\1\77\55\0\1\55"+
    "\52\0\1\100\63\0\1\101\54\0\1\70\54\0\1\102"+
    "\52\0\1\55\40\0\1\33\45\0\1\103\60\0\1\30"+
    "\54\0\1\104\50\0\1\105\45\0\1\106\61\0\1\55"+
    "\60\0\1\107\36\0\1\64\70\0\1\55\20\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[2322];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;
  private static final char[] EMPTY_BUFFER = new char[0];
  private static final int YYEOF = -1;
  private static java.io.Reader zzReader = null; // Fake

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\1\0\1\11\3\1\1\11\7\1\1\11\3\1\4\11"+
    "\1\1\4\0\1\11\5\0\1\11\2\1\1\11\1\0"+
    "\1\1\3\0\2\11\1\0\1\11\6\0\1\11\1\0"+
    "\1\11\7\0\1\1\1\0\1\1\7\0";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[71];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char[] zzBuffer = new char[0];

  /** this buffer may contains the current text array to be matched when it is cheap to acquire it */
  private char[] zzBufferArray;

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the textposition at the last state to be included in yytext */
  private int zzPushbackPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /**
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /* user code: */



  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  _HamletLexer(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  _HamletLexer(java.io.InputStream in) {
    this(new java.io.InputStreamReader
             (in, java.nio.charset.Charset.forName("UTF-8")));
  }

  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 132) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }

  public final int getTokenStart(){
    return zzStartRead;
  }

  public final int getTokenEnd(){
    return getTokenStart() + yylength();
  }

  public void reset(CharSequence buffer, int start, int end,int initialState){
    zzBuffer = buffer.toString().toCharArray();
    zzBufferArray = com.intellij.util.text.CharArrayUtil.fromSequenceWithoutCopying(buffer);
    zzCurrentPos = zzMarkedPos = zzStartRead = start;
    zzPushbackPos = 0;
    zzAtEOF  = false;
    zzAtBOL = true;
    zzEndRead = end;
    yybegin(initialState);
  }

  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   *
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {
    return true;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final CharSequence yytext() {
    return new String(zzBuffer, zzStartRead, zzMarkedPos);
  }


  /**
   * Returns the character at position <tt>pos</tt> from the
   * matched text.
   *
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch.
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBufferArray != null ? zzBufferArray[zzStartRead+pos]:zzBuffer[zzStartRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public IElementType advance() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char[] zzBufferL = zzBuffer;
    char[] zzBufferArrayL = zzBufferArray;
    char [] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = ZZ_LEXSTATE[zzLexicalState];

      // set up zzAction for empty match case:
      int zzAttributes = zzAttrL[zzState];
      if ( (zzAttributes & 1) == 1 ) {
        zzAction = zzState;
      }


      zzForAction: {
        while (true) {

          if (zzCurrentPosL < zzEndReadL)
            zzInput = zzBufferL[zzCurrentPosL++];
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = zzBufferL[zzCurrentPosL++];
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 1: 
          { return TokenType.BAD_CHARACTER;
          }
        case 23: break;
        case 2: 
          { return HamletTokenTypes.IDENTIFIER;
          }
        case 24: break;
        case 3: 
          { return TokenType.WHITE_SPACE;
          }
        case 25: break;
        case 4: 
          { return HamletTokenTypes.NEWLINE;
          }
        case 26: break;
        case 5: 
          { return HamletTokenTypes.OANGLE;
          }
        case 27: break;
        case 6: 
          { return HamletTokenTypes.ESCAPE;
          }
        case 28: break;
        case 7: 
          { return HamletTokenTypes.DOT;
          }
        case 29: break;
        case 8: 
          { return HamletTokenTypes.SLASH;
          }
        case 30: break;
        case 9: 
          { return HamletTokenTypes.END_INTERPOLATION;
          }
        case 31: break;
        case 10: 
          { return HamletTokenTypes.EQUAL;
          }
        case 32: break;
        case 11: 
          { return HamletTokenTypes.CANGLE;
          }
        case 33: break;
        case 12: 
          { return HamletTokenTypes.COMMENT;
          }
        case 34: break;
        case 13: 
          { return HamletTokenTypes.BINDSTATMENT;
          }
        case 35: break;
        case 14: 
          { return HamletTokenTypes.DOCTYPE;
          }
        case 36: break;
        case 15: 
          { return HamletTokenTypes.SHARP_IDENTIFIER;
          }
        case 37: break;
        case 16: 
          { return HamletTokenTypes.INTERPOLATION;
          }
        case 38: break;
        case 17: 
          { return HamletTokenTypes.DOT_IDENTIFIER;
          }
        case 39: break;
        case 18: 
          { return HamletTokenTypes.STRING;
          }
        case 40: break;
        case 19: 
          { return HamletTokenTypes.COMMENT_END;
          }
        case 41: break;
        case 20: 
          { return HamletTokenTypes.OPERATOR;
          }
        case 42: break;
        case 21: 
          { return HamletTokenTypes.COLON_IDENTIFIER;
          }
        case 43: break;
        case 22: 
          { return HamletTokenTypes.COMMENT_START;
          }
        case 44: break;
        default:
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
            return null;
          }
          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
