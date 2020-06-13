//. nsficon2qr.java
import java.io.*;
import java.util.*;
//import java.util.concurrent.*;
import java.awt.*;
import javax.servlet.*;
import javax.servlet.http.*;
import lotus.domino.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.imageio.*;
import javax.xml.parsers.*;
//import com.google.zxing.*;
//import com.google.zxing.common.*;
//import com.google.zxing.qrcode.*;

public class nsficon2qr extends HttpServlet{
  @Override
  protected void doGet( HttpServletRequest req, HttpServletResponse res ){
    try{
      NotesThread.sinitThread();
      Session session = NotesFactory.createSessionWithFullAccess();
      String servername = session.getServerName();

      String server = null;
      String filepath = req.getParameter( "filepath" );
      String _server = req.getParameter( "server" );
      if( _server != null && _server.length() > 0 ){
        server = _server;
      }
      if( filepath != null && filepath.length() > 0 ){
        Database db = session.getDatabase( server, filepath );
        if( !db.isOpen() ){
          db.open();
        }

        DxlExporter exporter = session.createDxlExporter();
        exporter.setOutputDOCTYPE( false );
        exporter.setConvertNotesBitmapsToGIF( true );
        String dxl = exporter.exportDxl( db );

        Element databaseElement = getRootElement( dxl );  //. <database>
        String repid = databaseElement.getAttribute( "replicaid" );

        //. DXL そのものを保存して、UTF-8 で読み込み直す
        Writer out1 = new OutputStreamWriter( new FileOutputStream( repid + ".xml" ), "UTF-8" );
        out1.write( dxl );
        out1.close();

        databaseElement = getRootElement( new File( repid + ".xml" ) );
        File file = new File( repid + ".xml" );
        file.deleteOnExit();

        //. DBタイトル
        String dbTitle = databaseElement.getAttribute( "title" );

        //. DB Icon
        int[] qrdata = new int[620];    //. 620?
        String dbicon = null;
        //. Lotus Notes 8.5.2 以降のエクスポートアイコンを探す
/*
        NodeList imgresList = databaseElement.getElementByTagName( "imageresource" );
        int nImgresList = imgresList.getLength();
        for( int i = 0; i < nImgresList; i ++ ){
          Element imgresElement = ( Element )imgresList.item( i );
          String imgresName = imgresElement.getAttribute( "name" );
          if( imgresName.equals( "$DBIcon" ) ){
            String imagename = imgresElement.getAttribute( "imagename" );
            int n = imagename.lastIndexOf( "." );
            String imageext = imagename.substring( n );  //. .gif, .jpg, .png, ..

            NodeList imgList = imgresElement.getElementsByTagName( "gif" );
            if( imgList.getLength() == 0 ){
              imgList = imgresElement.getElementsByTagName( "jpeg" );
            }

            byte[] data = null;
            if( imgList.getLength() > 0 ){
              Element imgElement = ( Element )imgList.item( 0 );
              String bitmapdata = imgElement.getFirstChild().getNodeValue();
              if( bitmapdata.length() > 0 ){
                data = Base64.decodeBase64( bitmapdata );
              }
            }
            if( data != null ){
              //. いったん保存
              try{
                File f = new File( repid + imageext );
                BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( f ) );
                bos.write( data, 0, data.length );
                bos.close();

                BufferedImage srcimg = null;
                try{
                  srcimg = ImageIO.read( f );
                  ByteArrayOutputStream os = new ByteArrayOutputStream();
                  ImageIO.write( srcimg, "gif", os );
                  dbicon = Base64.encodeBase64String( os.toByteArray() );
                }catch( Exception e ){
                  e.printStackTrace();
                  srcimg = null;
                }
              }catch( Exception e ){
                e.printStackTrace();
              }
            }
            break;
          }
        }
*/

        if( dbicon == null ){
          NodeList noteList = databaseElement.getElementsByTagName( "note" );
          int nNoteList = noteList.getLength();
          for( int cnt = 0; cnt < nNoteList; cnt ++ ){
            Element noteElement = ( Element )noteList.item( cnt );
            //String unid = getUnid( noteElement );

            //. クラシックアイコンの調査
            byte[] data = null;
            NodeList itemList = noteElement.getElementsByTagName( "item" );
            int nItemList = itemList.getLength();
            for( int j = 0; j < nItemList; j ++ ){
              Element itemElement = ( Element )itemList.item( j );
              String name = itemElement.getAttribute( "name" );
              if( name.toLowerCase().equals( "iconbitmap" ) ){
                NodeList list = itemElement.getElementsByTagName( "rawitemdata" );
                Element element = ( Element )list.item( 0 );
                String bitmapdata = element.getFirstChild().getNodeValue();
                //.System.out.println( "#bitmapdata = " + bitmapdata.length() ); //. 877
                bitmapdata = bitmapdata.replaceAll( "(\\r|\\n|\\t)", "" ); 
                Base64.Decoder decoder = Base64.getDecoder();
                data = decoder.decode( bitmapdata );  //. Illegal base64 character
                //data = Base64.decodeBase64( bitmapdata );
                break;
              }
            }

            if( data != null ){
              int[] dbicon_bg = new int[32*32];
              int[] dbicon_fg = new int[32*32];
              int[] dbicon_color = {
                159, //new Color( 0, 0, 0 ),
                145, //new Color( 255, 255, 255 ),  //. 背景
                14,  //new Color( 255, 0, 0 ),
                139, //new Color( 0, 255, 0 ),
                86,  //new Color( 0, 0, 255 ),
                50,  //new Color( 255, 0, 255 ),
                67,  //new Color( 255, 255, 0 ),
                141, //new Color( 0, 255, 255 ),
                17,  //new Color( 128, 0, 0 ),
                143, //new Color( 0, 128, 0 ),
                88,  //new Color( 0, 0, 128 ),
                43,  //new Color( 128, 0, 128 ),
                71,  //new Color( 128, 128, 0 ),
                124, //new Color( 0, 128, 128 ),
                //156, //new Color( 70, 70, 70 ),
                152, //new Color( 128, 128, 128 )
              };  //. #dbicon_color = 15

              int[] color_palette_table = new int[159];
              for( int i = 0; i < 16; i ++ ){
                for( int j = 0; j < 9; j ++ ){
                  int v = 16 * i + j;
                  color_palette_table[9*i+j] = v;
                }
              }
              for( int i = 0; i < 15; i ++ ){
                int v = 16 * i + 15;
                color_palette_table[16*9+i] = v;
              }

              //. background
              for( int j = 0x0006, k = 0; j <= 0x0085; j ++ ){
                int b = ( int )data[j];
                //. check by 1 bit
                for( int l = 0; l < 8; l ++ ){
                  int mask = 128 >> l;
                  int x = ( b & mask ) >> ( 7 - l );
                  dbicon_bg[k+l] = x;
                }
                k += 8;
              }

              //. foreground
              for( int j = 0x0086, k = 0; j <= 0x0285; j ++ ){
                int b = ( int )data[j];
                //. check by 4 bit
                for( int l = 0; l < 2; l ++ ){
                  int mask = 0xf0 >> ( l * 4 );
                  int x = ( b & mask ) >> ( 4 - ( l * 4 ) );
                  dbicon_fg[k+l] = x;
                }
                k += 2;
              }

              //. check all cell
              int[] imgdata = new int[512];
              for( int j = 0; j < 32 * 32; j += 2 ){
                int color_index1 = getColorIndex( dbicon_bg[j], dbicon_fg[j] );
                int color_index2 = getColorIndex( dbicon_bg[j+1], dbicon_fg[j+1] );

                //.クラシックアイコンは順序が逆
                //int color_index = color_index2 * 16 + color_index1;
                //imgdata[j/2] = color_index;
                int color_index = color_index1 * 16 + color_index2;
                imgdata[511-j/2] = color_index;
              }

              //. QR Code
              int idx = 0;

              //. Title
              int[] titlearray = str2bytearray( dbTitle );
              for( int i = 0; i < 42; i ++ ){
                if( i < titlearray.length ){
                  int c = titlearray[i];
                  qrdata[idx++] = c;
                }else{
                  qrdata[idx++] = 0;
                }
              }

              //. *1, *2
              qrdata[idx++] = 23;
              qrdata[idx++] = 146;

              //. Author
              int[] authorarray = str2bytearray( "Domino" );
              for( int i = 0; i < 18; i ++ ){
                if( i < authorarray.length ){
                  int c = authorarray[i];
                  qrdata[idx++] = c;
                }else{
                  qrdata[idx++] = 0;
                }
              }

              //. *12, *13, *3, *4
              qrdata[idx++] = 1;
              qrdata[idx++] = 0;
              qrdata[idx++] = 216;
              qrdata[idx++] = 144;

              //. Town
              int[] townarray = str2bytearray( servername );
              for( int i = 0; i < 18; i ++ ){
                if( i < townarray.length ){
                  int c = townarray[i];
                  qrdata[idx++] = c;
                }else{
                  qrdata[idx++] = 0;
                }
              }

              //. *14, *15, *5, *6
              qrdata[idx++] = 0;
              qrdata[idx++] = 0;
              qrdata[idx++] = 1;
              qrdata[idx++] = 2;

              //. Color palette
              for( int i = 0; i < 15; i ++ ){
                qrdata[idx++] = color_palette_table[dbicon_color[i]-1];
              }

              //. *7, *8, *9, *10, *11
              qrdata[idx++] = 198;
              qrdata[idx++] = 0;
              qrdata[idx++] = 9;
              qrdata[idx++] = 0;
              qrdata[idx++] = 0;

              //. palette data
              for( int i = 0; i < imgdata.length; i ++ ){
                qrdata[idx++] = imgdata[i];
              }
            }
          }
        }

        res.setContentType( "application/json; charset=UTF-8" );
        res.getWriter().println( Arrays.toString( qrdata ) );
      }else{
        res.setContentType( "text/plain; charset=UTF-8" );
        res.getWriter().println( "parameter filepath required." );
      }
    }catch( Exception e ){
      e.printStackTrace();
    }finally{
      NotesThread.stermThread();
    }
  }


  public Element getRootElement( String xml ){
    Element root = null;
    try{
      DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = dbfactory.newDocumentBuilder();
      org.w3c.dom.Document xdoc = builder.parse( new InputSource( new StringReader( xml ) ) );
      root = xdoc.getDocumentElement();
    }catch( Exception e ){
      e.printStackTrace();
    }

    return root;
  }

  public Element getRootElement( File file ){
    Element root = null;
    try{
      DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = dbfactory.newDocumentBuilder();
      org.w3c.dom.Document xdoc = builder.parse( file );
      root = xdoc.getDocumentElement();
    }catch( Exception e ){
      e.printStackTrace();
    }

    return root;
  }

  public int[] str2bytearray( String str ){
    int[] arr = new int[str.length()*2];
    for( int i = 0; i < str.length(); i ++ ){
      int cc = Character.codePointAt( str, i );
      int c1 = ( int )( cc / 256 );
      int c2 = cc % 256;
      arr[2*i] = c2;
      arr[2*i+1] = c1;
    }

    return arr;
  }

  public int getColorIndex( int bg, int fg ){
    int idx = -1;
    if( bg == 1 ){
      idx = 1;
    }else{
      idx = fg;
    }

    return idx;
  }
}

