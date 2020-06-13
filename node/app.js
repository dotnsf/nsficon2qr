//.  app.js
var express = require( 'express' ),
    bodyParser = require( 'body-parser' ),
    ejs = require( 'ejs' ),
    fs = require( 'fs' ),
    request = require( 'request' ),
    app = express();
var settings = require( './settings' );

var QRCode = require( 'qrcode' );
var uuidv1 = require( 'uuid/v1' );

app.use( bodyParser.urlencoded( { extended: true } ) );
app.use( bodyParser.json() );
app.use( express.static( __dirname + '/public' ) );

app.set( 'views', __dirname + '/views' );
app.set( 'view engine', 'ejs' );

app.get( '/', function( req, res ){
  res.render( 'index', { servlet_url: settings.servlet_url } );
});

app.get( '/nsficon', function( req, res ){
  res.contentType( 'application/json; charset=utf-8' );

  var filepath = req.query.filepath;
  if( filepath ){
    var server = ( req.query.server ? req.query.server : null );
    var url = settings.servlet_url + '?filepath=' + filepath;
    if( server ){
      url += ( '?server=' + server );
    }

    var option = {
      url: url,
      method: 'GET'
    };
    request( option, ( err0, res0, body0 ) => {
      if( err0 ){
        var p = JSON.stringify( { status: false, error: err0 }, null, 2 );
        res.status( 400 );
        res.write( p );
        res.end();
      }else{
        var p = JSON.stringify( { status: true, body: body0 }, null, 2 );
        res.write( p );
        res.end();
      }
    });
  }else{
    var p = JSON.stringify( { status: false, error: 'no filepath data.' }, null, 2 );
    res.status( 400 );
    res.write( p );
    res.end();
  }
});

app.post( '/qrcode', function( req, res ){
  res.contentType( 'application/json; charset=utf-8' );

  var data = JSON.parse( req.body.data );

  if( data ){
    var filepath = './tmp/' + uuidv1() + '.png';
    //. https://www.npmjs.com/package/qrcode
    QRCode.toFile( 
      filepath,
      [ { data: data, mode: 'byte' } ],
      function( err, result ){
        if( err ){
          console.log( err );
          fs.unlink( filepath, function( err ){} );
        }else{
          var bin = fs.readFileSync( filepath );
          fs.unlink( filepath, function( err ){} );
          res.contentType( 'image/png' );
          res.header( { 'Content-Disposition': 'inline' } );
          res.end( bin, 'binary' );
        }
      }
    );
  }else{
    var p = JSON.stringify( { status: false, error: 'no image data.' }, null, 2 );
    res.status( 400 );
    res.write( p );
    res.end();
  }
});

var port = process.env.PORT || 8080;
app.listen( port );
console.log( "server starting on " + port + " ..." );
