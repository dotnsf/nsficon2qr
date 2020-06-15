//. nsficon2qr.java
import java.io.*;
import java.util.*;
//import java.util.concurrent.*;
import java.awt.*;
import java.awt.image.*;
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

        //. DBIcon のみ
        NoteCollection nc = db.createNoteCollection( false );
        nc.selectAllFormatElements( true );
        nc.setSelectImageResources( true );
        nc.setSelectIcon( true );
        nc.buildCollection();

        DxlExporter exporter = session.createDxlExporter();
        exporter.setOutputDOCTYPE( false );
        exporter.setConvertNotesBitmapsToGIF( true );
        String dxl = exporter.exportDxl( nc ); //exporter.exportDxl( db );

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
        Boolean exporticon = false;

        //. Lotus Notes 8.5.2 以降のエクスポートアイコンを探す
        NodeList imgresList = databaseElement.getElementsByTagName( "imageresource" );
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
            if( imgList.getLength() == 0 ){
              imgList = imgresElement.getElementsByTagName( "png" );
            }

            byte[] data = null;
            if( imgList.getLength() > 0 ){
              Element imgElement = ( Element )imgList.item( 0 );
              String bitmapdata = imgElement.getFirstChild().getNodeValue();
              bitmapdata = bitmapdata.replaceAll( "(\\r|\\n|\\t)", "" ); 
              if( bitmapdata.length() > 0 ){
                Base64.Decoder decoder = Base64.getDecoder();
                data = decoder.decode( bitmapdata );
                //data = Base64.decodeBase64( bitmapdata );
              }
            }
            if( data != null ){
              //. エクスポートアイコンが見つかった
              try{
                //. https://stackoverflow.com/questions/12705385/how-to-convert-a-byte-to-a-bufferedimage-in-java
                ByteArrayInputStream bais = new ByteArrayInputStream( data );
                BufferedImage srcimg = null;
                try{
                  srcimg = ImageIO.read( bais );
                }catch( IOException e ){
                  e.printStackTrace();
                }

                if( srcimg != null ){
                  //. https://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image
                  Color[][] result = convertTo2D( srcimg );

                  //. カラーパレット変換テーブル
                  int[] color_palette_table = new int[159];
                  int _i = 0;
                  for( int ii = 0; ii < 16; ii ++ ){
                    for( int jj = 0; jj < 9; jj ++ ){
                      int v = 16 * ii + jj;
                      color_palette_table[_i++] = v;
                    }
                  }
                  for( int ii = 0; ii < 15; ii ++ ){
                    int v = 16 * ii + 15;
                    color_palette_table[_i++] = v;
                  }

                  //.
                  int __color_palette[][] = {
                    //. R    G    B
                    //{   0,   0,   0 },  //. dummy
                      { 255, 239, 255 },
                      { 255, 154, 173 },
                      { 239,  85, 156 },
                      { 255, 101, 173 },
                      { 255,   0,  99 },
                      { 189,  69, 115 },
                      { 206,   0,  82 },
                      { 156,   0,  49 },
                      {  82,  32,  49 },
                      { 255, 186, 206 },  //. 10
                      { 255, 117, 115 },
                      { 222,  48,  16 },
                      { 255,  85,  66 },
                      { 255,   0,   0 },
                      { 206, 101,  99 },
                      { 189,  69,  66 },
                      { 189,   0,   0 },
                      { 140,  32,  33 },
                      { 222, 207, 189 },
                      { 255, 207,  99 },  //. 20
                      { 222, 101,  33 },
                      { 222, 170,  33 },
                      { 222, 101,   0 },
                      { 189, 138,  82 },
                      { 222,  69,   0 },
                      { 189,  69,   0 },
                      {  99,  48,  16 },
                      { 255, 239, 222 },
                      { 255, 223, 206 },
                      { 255, 207, 173 },  //. 30
                      { 255, 186, 140 },
                      { 255, 170, 140 },
                      { 222, 138,  99 },
                      { 189, 101,  66 },
                      { 156,  85,  49 },
                      { 140,  69,  33 },
                      { 255, 207, 255 },
                      { 239, 138, 255 },
                      { 206, 101, 222 },
                      { 189, 138, 206 },  //. 40
                      { 206,   0, 255 },
                      { 156, 101, 156 },
                      { 140,   0, 173 },
                      {  82,   0, 115 },
                      {  49,   0,  66 },
                      { 255, 186, 255 },
                      { 255, 154, 255 },
                      { 222,  32, 189 },
                      { 222,  85, 239 },
                      { 255,   0, 206 },  //. 50
                      { 140,  85, 115 },
                      { 189,   0, 156 },
                      { 140,   0,  99 },
                      {  82,   0,  66 },
                      { 222, 186, 156 },
                      { 206, 170, 115 },
                      { 115,  69,  49 },
                      { 173, 117,  66 },
                      { 156,  48,   0 },
                      { 115,  48,  33 },  //. 60
                      {  82,  32,   0 },
                      {  49,  16,   0 },
                      {  33,  16,   0 },
                      { 255, 255, 206 },
                      { 255, 255, 115 },
                      { 222, 223,  33 },
                      { 255, 255,   0 },
                      { 255, 223,   0 },
                      { 206, 170,   0 },
                      { 156, 154,   0 },  //. 70
                      { 140, 117,   0 },
                      {  82,  85,   0 },
                      { 222, 186, 255 },
                      { 189, 154, 239 },
                      {  99,  48, 206 },
                      { 156,  85, 255 },
                      {  99,   0, 255 },
                      {  82,  69, 140 },
                      {  66,   0, 156 },
                      {  33,   0,  99 },  //. 80
                      {  33,  16,  49 },
                      { 189, 186, 255 },
                      { 140, 154, 255 },
                      {  49,  48, 173 },
                      {  49,  85, 239 },
                      {   0,   0, 255 },
                      {  49,  48, 140 },
                      {   0,   0, 173 },
                      {  16,  16,  99 },
                      {   0,   0,  33 },  //. 90
                      { 156, 239, 189 },
                      {  99, 207, 115 },
                      {  33, 101,  16 },
                      {  66, 170,  49 },
                      {   0, 138,  49 },
                      {  82, 117,  82 },
                      {  33,  85,   0 },
                      {  16,  48,  33 },
                      {   0,  32,  16 },
                      { 222, 255, 189 },  //. 100
                      { 206, 255, 140 },
                      { 140, 170,  82 },
                      { 173, 223, 140 },
                      { 140, 255,   0 },
                      { 173, 186, 156 },
                      {  99, 186,   0 },
                      {  82, 154,   0 },
                      {  49, 101,   0 },
                      { 189, 223, 255 },
                      { 115, 207, 255 },  //. 110
                      {  49,  85, 156 },
                      {  99, 154, 255 },
                      {  16, 117, 255 },
                      {  66, 117, 173 },
                      {  33,  69, 115 },
                      {   0,  32, 115 },
                      {   0,  16,  66 },
                      { 173, 255, 255 },
                      {  82, 255, 255 },
                      {   0, 138, 189 },  //. 120
                      {  82, 186, 206 },
                      {   0, 207, 255 },
                      {  66, 154, 173 },
                      {   0, 101, 140 },
                      {   0,  69,  82 },
                      {   0,  32,  49 },
                      { 206, 255, 239 },
                      { 173, 239, 222 },
                      {  49, 207, 173 },
                      {  82, 239, 189 },  //. 130
                      {   0, 255, 206 },
                      { 115, 170, 173 },
                      {   0, 170, 156 },
                      {   0, 138, 115 },
                      {   0,  69,  49 },
                      { 173, 255, 173 },
                      { 115, 255, 115 },
                      {  99, 223,  66 },
                      {   0, 255,   0 },
                      {  33, 223,  33 },  //. 140
                      {  82, 186,  82 },
                      {   0, 186,   0 },
                      {   0, 138,   0 },
                      {  33,  69,  33 },
                      { 255, 255, 255 },
                      { 239, 239, 239 },
                      { 222, 223, 222 },
                      { 206, 207, 206 },
                      { 189, 186, 189 },
                      { 173, 170, 173 },  //. 150
                      { 156, 154, 156 },
                      { 140, 138, 140 },
                      { 115, 117, 115 },
                      {  99, 101,  99 },
                      {  82,  85,  82 },
                      {  66,  69,  66 },
                      {  49,  48,  49 },
                      {  33,  32,  33 },
                      {   0,   0,   0 }
                  };

                  int[] tmpimgdata = new int[32*32];
                  for( int row = 0; row < 32; row ++ ){
                    for( int col = 0; col < 32; col ++ ){
                      Color color = result[row][col];
                      int r1 = color.getRed();
                      int g1 = color.getGreen();
                      int b1 = color.getBlue();
                      int a1 = color.getTransparency();
                      int color_index1 = getColorIndex( __color_palette, r1, g1, b1, a1 );

                      //. 減色処理前のデータ
                      tmpimgdata[row*32+col] = color_index1;
                    }
                  }

                  //. 減色の準備として色をカウント
                  int[] counts = new int[159];
                  for( int ii = 0; ii < 159; ii ++ ){
                    counts[ii] = 0;
                  }
                  for( int ii = 0; ii < tmpimgdata.length; ii ++ ){
                    counts[tmpimgdata[ii]] ++;
                  }
                  
                  //. 上位15色のみ有効にして取り出す
                  Integer[] ranks = new Integer[159];
                  for( int ii = 0; ii < counts.length; ii ++ ){
                    ranks[ii] = ( Integer )counts[ii];
                  }
                  Arrays.sort( ranks, Comparator.reverseOrder() );

                  //. 上位15位までの色のインデックス番号を取り出す
                  ArrayList<Integer> idx15 = new ArrayList<Integer>();
                  //. 15位の数値より大きいものだけをランキングへ
                  for( int ii = 0; ii < counts.length; ii ++ ){
                    //int r = ranks.indexOf( new Integer(counts[ii]) );
                    int r = indexOfIntegerArray( ranks, new Integer(counts[ii]) );
                    if( r > -1 && ( int )ranks[14] < counts[ii] ){
                      idx15.add( ii );
                    }
                  }
                  //. 15位の数値と同じものだけをランキングへ
                  for( int ii = 0; ii < counts.length && idx15.size() < 15; ii ++ ){
                    //int r = ranks.indexOf( new Integer(counts[ii]) );
                    int r = indexOfIntegerArray( ranks, new Integer(counts[ii]) );
                    if( r > -1 && ( int )ranks[14] == counts[ii] ){
                      //if( idx15.size() < 15 ){
                        idx15.add( ii );
                      //}
               	    }
                  }

                  //. 上位15位のカラーパレット
                  int[][] color_palette15 = new int[15][3];
                  for( int ii = 0; ii < idx15.size(); ii ++ ){
                    color_palette15[ii] = __color_palette[idx15.get(ii)];
                  }

                  //. 上位15色のみ有効にして、他の色を使っている場合は近い色にシフトする形で減色処理
                  //int[] tmpimgdata2 = new int[32*32];
                  ArrayList<Integer> tmpimgdata2 = new ArrayList<Integer>();
                  for( int k = 0; k < tmpimgdata.length; k ++ ){
                	  int color_index = tmpimgdata[k];
                  	if( idx15.indexOf( ( Integer )color_index ) > -1 ){
                  	  tmpimgdata2.add( idx15.indexOf( ( Integer )color_index ) );
                  	}else{
                	    //. 15色の中から近い色を探す
                	    int[] cp = __color_palette[color_index];

                      int idx = -1;  //. 0-158
                      //. ３次元ユークリッド距離で比較
                      int mx = 255 * 255 * 3;
                      for( int jj = 0; jj < color_palette15.length; jj ++ ){
                        int d = ( color_palette15[jj][0] - cp[0] ) * ( color_palette15[jj][0] - cp[0] )
                          + ( color_palette15[jj][1] - cp[1] ) * ( color_palette15[jj][1] - cp[1] )
                          + ( color_palette15[jj][2] - cp[2] ) * ( color_palette15[jj][2] - cp[2] );
                        if( d < mx ){
                          idx = i;
                          mx = d;
                        }
                	    }
                      tmpimgdata2.add( idx );
                    }
                  }

                  //. エンディアンを意識して再構成
                  int[] imgdata = new int[32*16];
                  for( int k = 0; k < tmpimgdata2.size(); k += 2 ){
                    int color_index1 = tmpimgdata2.get( k );
                    int color_index2 = tmpimgdata2.get( k+1 );
                    int idx = color_index2 * 16 + color_index1;
                	  imgdata[k/2] = idx;
                  }

                  //. QR Code
                  int idx = 0;

                  //. Title
                  int[] titlearray = str2bytearray( dbTitle );
                  for( int ii = 0; ii < 42; ii ++ ){
                    if( ii < titlearray.length ){
                      int c = titlearray[ii];
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
                  for( int ii = 0; ii < 18; ii ++ ){
                    if( ii < authorarray.length ){
                      int c = authorarray[ii];
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
                  for( int ii = 0; ii < 18; ii ++ ){
                    if( ii < townarray.length ){
                      int c = townarray[ii];
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
                  for( int ii = 0; ii < 15; ii ++ ){
                    qrdata[idx++] = color_palette_table[idx15.get(ii)];
                  }

                  //. *7, *8, *9, *10, *11
                  qrdata[idx++] = 198;
                  qrdata[idx++] = 0;
                  qrdata[idx++] = 9;
                  qrdata[idx++] = 0;
                  qrdata[idx++] = 0;

                  //. palette data
                  for( int ii = 0; ii < imgdata.length; ii ++ ){
                    qrdata[idx++] = imgdata[ii];
                  }

                  //. エクスポートアイコンだった
                  exporticon = true;
                }
              }catch( Exception e ){
                e.printStackTrace();
              }
            }
            break;
          }
        }

        if( exporticon == false ){
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
                data = decoder.decode( bitmapdata );
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

  public int getColorIndex( int[][] __color_palette, int rr, int gg, int bb, int aa ){
    int idx = -1;  //. 0-158

    //. ３次元ユークリッド距離で比較
    int mx = 255 * 255 * 3;
    for( int i = 0; i < __color_palette.length; i ++ ){
      int d = ( __color_palette[i][0] - rr ) * ( __color_palette[i][0] - rr )
        + ( __color_palette[i][1] - gg ) * ( __color_palette[i][1] - gg )
        + ( __color_palette[i][2] - bb ) * ( __color_palette[i][2] - bb );
      if( d < mx ){
        idx = i;
        mx = d;
      }
    }

    return idx;
  }


  public static Color[][] convertTo2D( BufferedImage image ){
    int width = image.getWidth();
    int height = image.getHeight();
    Color[][] result = new Color[height][width];

    for( int row = 0; row < height; row ++ ){
      for( int col = 0; col < width; col ++ ){
        result[row][col] = new Color( image.getRGB( col, row ) );
      }
    }

    return result;
  }

  public static int indexOfIntegerArray( Integer[] array, Integer key ){
    int r = -1;
    for( int i = 0; i < array.length; i ++ ){
      if( ( int )key == ( int )array[i] ){
        r = i;
        break;
      }
    }
    return r;
  }
}

