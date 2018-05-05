'use strict';
var port = process.env.PORT || 8081;
var express = require('express');
var app = express();
var https = require('https');
var key = 'GOOGLE_API_KEY';
var yelpkey = 'YELP_KEY';

const yelp = require('yelp-fusion');
const client = yelp.client(yelpkey);

app.use(express.urlencoded());
app.get('/', function (req, res) {
    res.sendFile(__dirname + '/index.html');
});

app.get('/places', function (req, res) {
    var distance = req.query.distance;
    if (distance == '')
        distance = 10 * 1609.344;
    else
        distance *= 1609.344;
    var location;
    if (req.query.location == "Here")
        location = req.query.ipLocation;
    else
        location = req.query.ipOther;
    var placesUrl = 'https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=' + location + '&radius=' + distance + '&type=' + (req.query.category) + '&keyword=' + encodeURI(req.query.keyword) + '&key=' + key;
    var request = https.get(placesUrl, placesRes => {
        let body = '';
        placesRes.on('data', data => {
            body += data;
        });
        placesRes.on('end', () => {
            res.send(JSON.parse(body));
        });
    });
});
app.get('/geocode', function (req, res) {
    var location;
    var geocodeUrl = 'https://maps.googleapis.com/maps/api/geocode/json?address=' + encodeURI(req.query.otherLocation) + '&key=' + key;
    var geocodeRequest = https.get(geocodeUrl, geocodeRes => {
        let body = '';
        geocodeRes.on('data', data => {
            body += data;
        });
        geocodeRes.on('end', () => {
            res.send(JSON.parse(body));
        });
    });
});
app.get('/yelpMatch', function (req, res) {
    
    var name = req.query.name;
    var address1 = "";
    if (req.query.street_number)
        address1 = req.query.street_number+" ";
    if (req.query.route)
        address1+= req.query.route+" ";
    if (req.query.locality)
        address1+= req.query.locality;
    var city = "";
    if (req.query.administrative_area_level_2)
        city = req.query.administrative_area_level_2;
    var state = "";
    if (req.query.administrative_area_level_1)
        state = req.query.administrative_area_level_1;
    var country = "";
    if (req.query.country)
        country = req.query.country;
    var postal_code="";
    if (req.query.postal_code)
        postal_code=req.query.postal_code;
    
    client.businessMatch('best', {
        name: name,
        address1:address1,
        city: city,
        state: state,
        country: country,
        postal_code: postal_code
    }).then(response => {
        res.send(response.jsonBody);
    }).catch(e => {
        console.log(e);
    });
});

app.get('/yelpReviews', function (req, res) {
    var id = req.query.id;
    client.reviews(id).then(response => {
        res.send(response.jsonBody);
    }).catch(e => {
        console.log(e);
    });
});
app.get('/nextPage', function (req, res) {
    var nextPageUrl = 'https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken='+req.query.next_page_token+'&key=' + key;
    var nextPageReq = https.get(nextPageUrl, nextPageRes => {
        let body = '';
        nextPageRes.on('data', data => {
            body += data;
        });
        nextPageRes.on('end', () => {
            res.send(JSON.parse(body));
        });
    });
});
app.get('/details', function (req, res) {
    var nextPageUrl = 'https://maps.googleapis.com/maps/api/place/details/json?placeid='+req.query.place_id+'&key=' + key;
    var nextPageReq = https.get(nextPageUrl, nextPageRes => {
        let body = '';
        nextPageRes.on('data', data => {
            body += data;
        });
        nextPageRes.on('end', () => {
            res.send(JSON.parse(body));
        });
    });
});
app.get('/mapDirection', function (req, res) {
    var nextPageUrl = 'https://maps.googleapis.com/maps/api/directions/json?origin='+req.query.origin+'&destination='+req.query.destination+'&mode='+req.query.mode+'&key='+key;
    var nextPageReq = https.get(nextPageUrl, nextPageRes => {
        let body = '';
        nextPageRes.on('data', data => {
            body += data;
        });
        nextPageRes.on('end', () => {
            res.send(JSON.parse(body));
        });
    });
});
app.listen(port);

