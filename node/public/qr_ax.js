//. qr_ax.js

function str2bytearray( str ){
  var arr = [];
  for( var i = 0; i < str.length; i ++ ){
    var cc = str.charCodeAt( i );
    var c1 = Math.floor( cc / 256 );
    var c2 = cc % 256;
    arr.push( c2 );
    arr.push( c1 );
  }

  return arr;
}

//. カラーパレット
var __color_palette = [
//. R    G    B
//[   0,   0,   0 ],  //. dummy
  [ 255, 239, 255 ],
  [ 255, 154, 173 ],
  [ 239,  85, 156 ],
  [ 255, 101, 173 ],
  [ 255,   0,  99 ],
  [ 189,  69, 115 ],
  [ 206,   0,  82 ],
  [ 156,   0,  49 ],
  [  82,  32,  49 ],
  [ 255, 186, 206 ],  //. 10
  [ 255, 117, 115 ],
  [ 222,  48,  16 ],
  [ 255,  85,  66 ],
  [ 255,   0,   0 ],
  [ 206, 101,  99 ],
  [ 189,  69,  66 ],
  [ 189,   0,   0 ],
  [ 140,  32,  33 ],
  [ 222, 207, 189 ],
  [ 255, 207,  99 ],  //. 20
  [ 222, 101,  33 ],
  [ 222, 170,  33 ],
  [ 222, 101,   0 ],
  [ 189, 138,  82 ],
  [ 222,  69,   0 ],
  [ 189,  69,   0 ],
  [  99,  48,  16 ],
  [ 255, 239, 222 ],
  [ 255, 223, 206 ],
  [ 255, 207, 173 ],  //. 30
  [ 255, 186, 140 ],
  [ 255, 170, 140 ],
  [ 222, 138,  99 ],
  [ 189, 101,  66 ],
  [ 156,  85,  49 ],
  [ 140,  69,  33 ],
  [ 255, 207, 255 ],
  [ 239, 138, 255 ],
  [ 206, 101, 222 ],
  [ 189, 138, 206 ],  //. 40
  [ 206,   0, 255 ],
  [ 156, 101, 156 ],
  [ 140,   0, 173 ],
  [  82,   0, 115 ],
  [  49,   0,  66 ],
  [ 255, 186, 255 ],
  [ 255, 154, 255 ],
  [ 222,  32, 189 ],
  [ 222,  85, 239 ],
  [ 255,   0, 206 ],  //. 50
  [ 140,  85, 115 ],
  [ 189,   0, 156 ],
  [ 140,   0,  99 ],
  [  82,   0,  66 ],
  [ 222, 186, 156 ],
  [ 206, 170, 115 ],
  [ 115,  69,  49 ],
  [ 173, 117,  66 ],
  [ 156,  48,   0 ],
  [ 115,  48,  33 ],  //. 60
  [  82,  32,   0 ],
  [  49,  16,   0 ],
  [  33,  16,   0 ],
  [ 255, 255, 206 ],
  [ 255, 255, 115 ],
  [ 222, 223,  33 ],
  [ 255, 255,   0 ],
  [ 255, 223,   0 ],
  [ 206, 170,   0 ],
  [ 156, 154,   0 ],  //. 70
  [ 140, 117,   0 ],
  [  82,  85,   0 ],
  [ 222, 186, 255 ],
  [ 189, 154, 239 ],
  [  99,  48, 206 ],
  [ 156,  85, 255 ],
  [  99,   0, 255 ],
  [  82,  69, 140 ],
  [  66,   0, 156 ],
  [  33,   0,  99 ],  //. 80
  [  33,  16,  49 ],
  [ 189, 186, 255 ],
  [ 140, 154, 255 ],
  [  49,  48, 173 ],
  [  49,  85, 239 ],
  [   0,   0, 255 ],
  [  49,  48, 140 ],
  [   0,   0, 173 ],
  [  16,  16,  99 ],
  [   0,   0,  33 ],  //. 90
  [ 156, 239, 189 ],
  [  99, 207, 115 ],
  [  33, 101,  16 ],
  [  66, 170,  49 ],
  [   0, 138,  49 ],
  [  82, 117,  82 ],
  [  33,  85,   0 ],
  [  16,  48,  33 ],
  [   0,  32,  16 ],
  [ 222, 255, 189 ],  //. 100
  [ 206, 255, 140 ],
  [ 140, 170,  82 ],
  [ 173, 223, 140 ],
  [ 140, 255,   0 ],
  [ 173, 186, 156 ],
  [  99, 186,   0 ],
  [  82, 154,   0 ],
  [  49, 101,   0 ],
  [ 189, 223, 255 ],
  [ 115, 207, 255 ],  //. 110
  [  49,  85, 156 ],
  [  99, 154, 255 ],
  [  16, 117, 255 ],
  [  66, 117, 173 ],
  [  33,  69, 115 ],
  [   0,  32, 115 ],
  [   0,  16,  66 ],
  [ 173, 255, 255 ],
  [  82, 255, 255 ],
  [   0, 138, 189 ],  //. 120
  [  82, 186, 206 ],
  [   0, 207, 255 ],
  [  66, 154, 173 ],
  [   0, 101, 140 ],
  [   0,  69,  82 ],
  [   0,  32,  49 ],
  [ 206, 255, 239 ],
  [ 173, 239, 222 ],
  [  49, 207, 173 ],
  [  82, 239, 189 ],  //. 130
  [   0, 255, 206 ],
  [ 115, 170, 173 ],
  [   0, 170, 156 ],
  [   0, 138, 115 ],
  [   0,  69,  49 ],
  [ 173, 255, 173 ],
  [ 115, 255, 115 ],
  [  99, 223,  66 ],
  [   0, 255,   0 ],
  [  33, 223,  33 ],  //. 140
  [  82, 186,  82 ],
  [   0, 186,   0 ],
  [   0, 138,   0 ],
  [  33,  69,  33 ],
  [ 255, 255, 255 ],
  [ 239, 239, 239 ],
  [ 222, 223, 222 ],
  [ 206, 207, 206 ],
  [ 189, 186, 189 ],
  [ 173, 170, 173 ],  //. 150
  [ 156, 154, 156 ],
  [ 140, 138, 140 ],
  [ 115, 117, 115 ],
  [  99, 101,  99 ],
  [  82,  85,  82 ],
  [  66,  69,  66 ],
  [  49,  48,  49 ],
  [  33,  32,  33 ],
  [   0,   0,   0 ]
];

function getColorIndex( rr, gg, bb, aa ){
  var idx = -1;  //. 0-158

  //. ３次元ユークリッド距離で比較
  var mx = 255 * 255 * 3;
  for( var i = 0; i < __color_palette.length; i ++ ){
    var d = ( __color_palette[i][0] - rr ) * ( __color_palette[i][0] - rr )
      + ( __color_palette[i][1] - gg ) * ( __color_palette[i][1] - gg )
      + ( __color_palette[i][2] - bb ) * ( __color_palette[i][2] - bb );
    if( d < mx ){
      idx = i;
      mx = d;
    }
  }

  return idx;
}

function qrCode( canvas_id, title, author, town ){
  var data = [];

  var canvas = document.getElementById( canvas_id );
  if( !canvas || !canvas.getContext ){
    return false;
  }
  var ctx = canvas.getContext( '2d' );

  //. カラーパレット変換テーブル
  var color_palette_table = [];
  for( var i = 0; i < 16; i ++ ){
    for( var j = 0; j < 9; j ++ ){
      var v = 16 * i + j;
      color_palette_table.push( v );
    }
  }
  for( var i = 0; i < 15; i ++ ){
    var v = 16 * i + 15;
    color_palette_table.push( v );
  }

  //. 画像データ
  var tmpimgdata = [];
  var imagedata = ctx.getImageData( 0, 0, 32, 32 );
  for( var i = 0; i < imagedata.height; i ++ ){
    for( var j = 0; j < imagedata.width; j ++ ){
      var idx = ( j + i * imagedata.width ) * 4;

  	  var r1 = imagedata.data[idx];
  	  var g1 = imagedata.data[idx+1];
      var b1 = imagedata.data[idx+2];
      var a1 = imagedata.data[idx+3];
      var color_index1 = getColorIndex( r1, g1, b1, a1 );

	    //. 減色処理前のデータ
	    tmpimgdata.push( color_index1 );
    }
  }

  //. （減色の準備としての）色カウント
  var counts = [];
  for( var i = 0; i < 159; i ++ ){
    counts.push( 0 );
  }
  for( var i = 0; i < tmpimgdata.length; i ++ ){
    counts[tmpimgdata[i]] ++;
  }

  //. 上位15色のみ有効にして取り出す
  var ranks = JSON.parse( JSON.stringify( counts ) );
  ranks.sort(
    function( a, b ){ return ( a < b ? 1 : -1 ); }
  );
  ranks = ranks.slice( 15 );  //. 上位15までの色の使われている数

  //. 上位15位までの色のインデックス番号を取り出す
  var idx15 = [];
  //. 15位の数値より大きいものだけをランキングへ
  for( var i = 0; i < counts.length; i ++ ){
    if( ranks.indexOf( counts[i] ) > -1 ){
      if( ranks[14] < counts[i] ){
        idx15.push( i );
	    }
	  }
  }
  //. 15位の数値と同じものだけをランキングへ
  for( var i = 0; i < counts.length; i ++ ){
    if( ranks.indexOf( counts[i] ) > -1 ){
      if( ranks[14] == counts[i] ){
        if( idx15.length < 15 ){
          idx15.push( i );
		    }
	    }
	  }
  }

  //. 上位15位のカラーパレット
  var color_palette15 = [];
  for( var i = 0; i < idx15.length; i ++ ){
    color_palette15.push( __color_palette[idx15[i]] );
  }

  //. 上位15色のみ有効にして、他の色を使っている場合は近い色にシフトする形で減色処理
  var tmpimgdata2 = [];
  for( var k = 0; k < tmpimgdata.length; k ++ ){
	var color_index = tmpimgdata[k];
	if( idx15.indexOf( color_index ) > -1 ){
	  tmpimgdata2.push( idx15.indexOf( color_index ) );
	}else{
	  //. 15色の中から近い色を探す
	  var cp = __color_palette[color_index];

      var idx = -1;  //. 0-158
      //. ３次元ユークリッド距離で比較
      var mx = 255 * 255 * 3;
      for( var i = 0; i < color_palette15.length; i ++ ){
        var d = ( color_palette15[i][0] - cp[0] ) * ( color_palette15[i][0] - cp[0] )
          + ( color_palette15[i][1] - cp[1] ) * ( color_palette15[i][1] - cp[1] )
          + ( color_palette15[i][2] - cp[2] ) * ( color_palette15[i][2] - cp[2] );
        if( d < mx ){
          idx = i;
          mx = d;
        }
	    }
      tmpimgdata2.push( idx );
	  }
  }

  //. エンディアンを意識して再構成
  var imgdata = [];
  for( var i = 0; i < tmpimgdata2.length; i += 2 ){
    var color_index1 = tmpimgdata2[i];
    var color_index2 = tmpimgdata2[i+1];
    var idx = color_index2 * 16 + color_index1;
	  imgdata.push( idx );
  }

  if( color_palette15 && imgdata ){
    //. Title
    var titlearray = str2bytearray( title );
    for( var i = 0; i < titlearray.length || i < 42; i ++ ){
      if( i < 42 ){
        if( i < titlearray.length ){
          var c = titlearray[i];
          data.push( c );
        }else{
          data.push( 0 );
        }
      }
    }

    //. *1, *2
    data.push( 23 );
    data.push( 146 );

    //. Author
    var authorarray = str2bytearray( author );
    for( var i = 0; i < authorarray.length || i < 18; i ++ ){
      if( i < 18 ){
        if( i < authorarray.length ){
          var c = authorarray[i];
          data.push( c );
        }else{
          data.push( 0 );
        }
      }
    }

    //. *12, *13, *3, *4
    data.push( 1 );
    data.push( 0 );
    data.push( 216 );
    data.push( 144 );

    //. Town
    var townarray = str2bytearray( town );
    for( var i = 0; i < townarray.length || i < 18; i ++ ){
      if( i < 18 ){
        if( i < townarray.length ){
          var c = townarray[i];
          data.push( c );
        }else{
          data.push( 0 );
        }
      }
    }

    //. *14, *15, *5, *6
    data.push( 0 );
    data.push( 0 );
    data.push( 1 );
    data.push( 2 );

	  //. Color Pallete
    for( var i = 0; i < color_palette15.length; i ++ ){
      data.push( color_palette_table[idx15[i]] );
    }

    //. *7, *8, *9, *10, *11
    data.push( 198 );
    data.push( 0 );
    data.push( 9 );
    data.push( 0 );
    data.push( 0 );

    //. Palette data
    for( var i = 0; i < imgdata.length; i ++ ){
      data.push( imgdata[i] );
	  }
  }

  return data;
}

function data2qrcode( data, postpath, img_id ){
  //. 結果を blob で受け取るため、XMLHttpRequest で送信
  //. https://qiita.com/Yarimizu14/items/f56123c738f12ad1844a
  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function(){
    if( this.readyState == 4 && this.status == 200 ){
      var data = this.response;
      var reader = new FileReader();
      reader.onloadend = function(){
        console.log( reader.result );
        var qrcode_img = document.getElementById( img_id );
        qrcode_img.src = reader.result;
      }
      reader.readAsDataURL( data );
    }
  }
  xhr.open( 'POST', postpath );
  xhr.responseType = 'blob';
  xhr.setRequestHeader( 'Content-Type', 'application/x-www-form-urlencoded' );
  xhr.send( 'data=' + data );
}
