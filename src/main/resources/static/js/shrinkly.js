var shrinklyUtil = {

  getTruncatedUrl: function(url) {
    url = _.replace(url, 'http://', '');
    url = _.replace(url, 'https://', '');
    return _.truncate(url, {
      'length': 35,
      'omission': ' ...',
      'separator': /\/+/
    });
  },

  getRandomSubtitle: function () {
    var titles = ['your grass pink', 'your ice hockey rink', 'your hyperlink', 'your whole universe', 'all the things',
      'what you want', 'your brand',
      'your whole kitchen sink', 'your link purplish pink', 'your whole precinct', 'your link in a blink', 'your life'];

    return _.sample(titles);
  },

  getDomainRegex: function () {
    var domainRegex = /^.*(\.aaa|\.aarp|\.abarth|\.abb|\.abbott|\.abbvie|\.abc|\.able|\.abogado|\.abudhabi|\.ac|\.academy|\.accenture|\.accountant|\.accountants|\.aco|\.actor|\.ad|\.adac|\.ads|\.adult|\.ae|\.aeg|\.aero|\.aetna|\.af|\.afamilycompany|\.afl|\.africa|\.ag|\.agakhan|\.agency|\.ai|\.aig|\.aigo|\.airbus|\.airforce|\.airtel|\.akdn|\.al|\.alfaromeo|\.alibaba|\.alipay|\.allfinanz|\.allstate|\.ally|\.alsace|\.alstom|\.am|\.americanexpress|\.americanfamily|\.amex|\.amfam|\.amica|\.amsterdam|\.analytics|\.android|\.anquan|\.anz|\.ao|\.aol|\.apartments|\.app|\.apple|\.aq|\.aquarelle|\.ar|\.arab|\.aramco|\.archi|\.army|\.arpa|\.art|\.arte|\.as|\.asda|\.asia|\.associates|\.at|\.athleta|\.attorney|\.au|\.auction|\.audi|\.audible|\.audio|\.auspost|\.author|\.auto|\.autos|\.avianca|\.aw|\.aws|\.ax|\.axa|\.az|\.azure|\.ba|\.baby|\.baidu|\.banamex|\.bananarepublic|\.band|\.bank|\.bar|\.barcelona|\.barclaycard|\.barclays|\.barefoot|\.bargains|\.baseball|\.basketball|\.bauhaus|\.bayern|\.bb|\.bbc|\.bbt|\.bbva|\.bcg|\.bcn|\.bd|\.be|\.beats|\.beauty|\.beer|\.bentley|\.berlin|\.best|\.bestbuy|\.bet|\.bf|\.bg|\.bh|\.bharti|\.bi|\.bible|\.bid|\.bike|\.bing|\.bingo|\.bio|\.biz|\.bj|\.black|\.blackfriday|\.blockbuster|\.blog|\.bloomberg|\.blue|\.bm|\.bms|\.bmw|\.bn|\.bnl|\.bnpparibas|\.bo|\.boats|\.boehringer|\.bofa|\.bom|\.bond|\.boo|\.book|\.booking|\.bosch|\.bostik|\.boston|\.bot|\.boutique|\.box|\.br|\.bradesco|\.bridgestone|\.broadway|\.broker|\.brother|\.brussels|\.bs|\.bt|\.budapest|\.bugatti|\.build|\.builders|\.business|\.buy|\.buzz|\.bv|\.bw|\.by|\.bz|\.bzh|\.ca|\.cab|\.cafe|\.cal|\.call|\.calvinklein|\.cam|\.camera|\.camp|\.cancerresearch|\.canon|\.capetown|\.capital|\.capitalone|\.car|\.caravan|\.cards|\.care|\.career|\.careers|\.cars|\.cartier|\.casa|\.case|\.caseih|\.cash|\.casino|\.cat|\.catering|\.catholic|\.cba|\.cbn|\.cbre|\.cbs|\.cc|\.cd|\.ceb|\.center|\.ceo|\.cern|\.cf|\.cfa|\.cfd|\.cg|\.ch|\.chanel|\.channel|\.charity|\.chase|\.chat|\.cheap|\.chintai|\.christmas|\.chrome|\.chrysler|\.church|\.ci|\.cipriani|\.circle|\.cisco|\.citadel|\.citi|\.citic|\.city|\.cityeats|\.ck|\.cl|\.claims|\.cleaning|\.click|\.clinic|\.clinique|\.clothing|\.cloud|\.club|\.clubmed|\.cm|\.cn|\.co|\.coach|\.codes|\.coffee|\.college|\.cologne|\.com|\.comcast|\.commbank|\.community|\.company|\.compare|\.computer|\.comsec|\.condos|\.construction|\.consulting|\.contact|\.contractors|\.cooking|\.cookingchannel|\.cool|\.coop|\.corsica|\.country|\.coupon|\.coupons|\.courses|\.cr|\.credit|\.creditcard|\.creditunion|\.cricket|\.crown|\.crs|\.cruise|\.cruises|\.csc|\.cu|\.cuisinella|\.cv|\.cw|\.cx|\.cy|\.cymru|\.cyou|\.cz|\.dabur|\.dad|\.dance|\.data|\.date|\.dating|\.datsun|\.day|\.dclk|\.dds|\.de|\.deal|\.dealer|\.deals|\.degree|\.delivery|\.dell|\.deloitte|\.delta|\.democrat|\.dental|\.dentist|\.desi|\.design|\.dev|\.dhl|\.diamonds|\.diet|\.digital|\.direct|\.directory|\.discount|\.discover|\.dish|\.diy|\.dj|\.dk|\.dm|\.dnp|\.do|\.docs|\.doctor|\.dodge|\.dog|\.domains|\.dot|\.download|\.drive|\.dtv|\.dubai|\.duck|\.dunlop|\.duns|\.dupont|\.durban|\.dvag|\.dvr|\.dz|\.earth|\.eat|\.ec|\.eco|\.edeka|\.edu|\.education|\.ee|\.eg|\.email|\.emerck|\.energy|\.engineer|\.engineering|\.enterprises|\.epson|\.equipment|\.er|\.ericsson|\.erni|\.es|\.esq|\.estate|\.esurance|\.et|\.etisalat|\.eu|\.eurovision|\.eus|\.events|\.everbank|\.exchange|\.expert|\.exposed|\.express|\.extraspace|\.fage|\.fail|\.fairwinds|\.faith|\.family|\.fan|\.fans|\.farm|\.farmers|\.fashion|\.fast|\.fedex|\.feedback|\.ferrari|\.ferrero|\.fi|\.fiat|\.fidelity|\.fido|\.film|\.final|\.finance|\.financial|\.fire|\.firestone|\.firmdale|\.fish|\.fishing|\.fit|\.fitness|\.fj|\.fk|\.flickr|\.flights|\.flir|\.florist|\.flowers|\.fly|\.fm|\.fo|\.foo|\.food|\.foodnetwork|\.football|\.ford|\.forex|\.forsale|\.forum|\.foundation|\.fox|\.fr|\.free|\.fresenius|\.frl|\.frogans|\.frontdoor|\.frontier|\.ftr|\.fujitsu|\.fujixerox|\.fun|\.fund|\.furniture|\.futbol|\.fyi|\.ga|\.gal|\.gallery|\.gallo|\.gallup|\.game|\.games|\.gap|\.garden|\.gb|\.gbiz|\.gd|\.gdn|\.ge|\.gea|\.gent|\.genting|\.george|\.gf|\.gg|\.ggee|\.gh|\.gi|\.gift|\.gifts|\.gives|\.giving|\.gl|\.glade|\.glass|\.gle|\.global|\.globo|\.gm|\.gmail|\.gmbh|\.gmo|\.gmx|\.gn|\.godaddy|\.gold|\.goldpoint|\.golf|\.goo|\.goodyear|\.goog|\.google|\.gop|\.got|\.gov|\.gp|\.gq|\.gr|\.grainger|\.graphics|\.gratis|\.green|\.gripe|\.grocery|\.group|\.gs|\.gt|\.gu|\.guardian|\.gucci|\.guge|\.guide|\.guitars|\.guru|\.gw|\.gy|\.hair|\.hamburg|\.hangout|\.haus|\.hbo|\.hdfc|\.hdfcbank|\.health|\.healthcare|\.help|\.helsinki|\.here|\.hermes|\.hgtv|\.hiphop|\.hisamitsu|\.hitachi|\.hiv|\.hk|\.hkt|\.hm|\.hn|\.hockey|\.holdings|\.holiday|\.homedepot|\.homegoods|\.homes|\.homesense|\.honda|\.horse|\.hospital|\.host|\.hosting|\.hot|\.hoteles|\.hotels|\.hotmail|\.house|\.how|\.hr|\.hsbc|\.ht|\.hu|\.hughes|\.hyatt|\.hyundai|\.ibm|\.icbc|\.ice|\.icu|\.id|\.ie|\.ieee|\.ifm|\.ikano|\.il|\.im|\.imamat|\.imdb|\.immo|\.immobilien|\.in|\.inc|\.industries|\.infiniti|\.info|\.ing|\.ink|\.institute|\.insurance|\.insure|\.int|\.intel|\.international|\.intuit|\.investments|\.io|\.ipiranga|\.iq|\.ir|\.irish|\.is|\.iselect|\.ismaili|\.ist|\.istanbul|\.it|\.itau|\.itv|\.iveco|\.jaguar|\.java|\.jcb|\.jcp|\.je|\.jeep|\.jetzt|\.jewelry|\.jio|\.jll|\.jm|\.jmp|\.jnj|\.jo|\.jobs|\.joburg|\.jot|\.joy|\.jp|\.jpmorgan|\.jprs|\.juegos|\.juniper|\.kaufen|\.kddi|\.ke|\.kerryhotels|\.kerrylogistics|\.kerryproperties|\.kfh|\.kg|\.kh|\.ki|\.kia|\.kim|\.kinder|\.kindle|\.kitchen|\.kiwi|\.km|\.kn|\.koeln|\.komatsu|\.kosher|\.kp|\.kpmg|\.kpn|\.kr|\.krd|\.kred|\.kuokgroup|\.kw|\.ky|\.kyoto|\.kz|\.la|\.lacaixa|\.ladbrokes|\.lamborghini|\.lamer|\.lancaster|\.lancia|\.lancome|\.land|\.landrover|\.lanxess|\.lasalle|\.lat|\.latino|\.latrobe|\.law|\.lawyer|\.lb|\.lc|\.lds|\.lease|\.leclerc|\.lefrak|\.legal|\.lego|\.lexus|\.lgbt|\.li|\.liaison|\.lidl|\.life|\.lifeinsurance|\.lifestyle|\.lighting|\.like|\.lilly|\.limited|\.limo|\.lincoln|\.linde|\.link|\.lipsy|\.live|\.living|\.lixil|\.lk|\.llc|\.loan|\.loans|\.locker|\.locus|\.loft|\.lol|\.london|\.lotte|\.lotto|\.love|\.lpl|\.lplfinancial|\.lr|\.ls|\.lt|\.ltd|\.ltda|\.lu|\.lundbeck|\.lupin|\.luxe|\.luxury|\.lv|\.ly|\.ma|\.macys|\.madrid|\.maif|\.maison|\.makeup|\.man|\.management|\.mango|\.map|\.market|\.marketing|\.markets|\.marriott|\.marshalls|\.maserati|\.mattel|\.mba|\.mc|\.mckinsey|\.md|\.me|\.med|\.media|\.meet|\.melbourne|\.meme|\.memorial|\.men|\.menu|\.merckmsd|\.metlife|\.mg|\.mh|\.miami|\.microsoft|\.mil|\.mini|\.mint|\.mit|\.mitsubishi|\.mk|\.ml|\.mlb|\.mls|\.mm|\.mma|\.mn|\.mo|\.mobi|\.mobile|\.mobily|\.moda|\.moe|\.moi|\.mom|\.monash|\.money|\.monster|\.mopar|\.mormon|\.mortgage|\.moscow|\.moto|\.motorcycles|\.mov|\.movie|\.movistar|\.mp|\.mq|\.mr|\.ms|\.msd|\.mt|\.mtn|\.mtr|\.mu|\.museum|\.mutual|\.mv|\.mw|\.mx|\.my|\.mz|\.na|\.nab|\.nadex|\.nagoya|\.name|\.nationwide|\.natura|\.navy|\.nba|\.nc|\.ne|\.nec|\.net|\.netbank|\.netflix|\.network|\.neustar|\.new|\.newholland|\.news|\.next|\.nextdirect|\.nexus|\.nf|\.nfl|\.ng|\.ngo|\.nhk|\.ni|\.nico|\.nike|\.nikon|\.ninja|\.nissan|\.nissay|\.nl|\.no|\.nokia|\.northwesternmutual|\.norton|\.now|\.nowruz|\.nowtv|\.np|\.nr|\.nra|\.nrw|\.ntt|\.nu|\.nyc|\.nz|\.obi|\.observer|\.off|\.office|\.okinawa|\.olayan|\.olayangroup|\.oldnavy|\.ollo|\.om|\.omega|\.one|\.ong|\.onl|\.online|\.onyourside|\.ooo|\.open|\.oracle|\.orange|\.org|\.organic|\.origins|\.osaka|\.otsuka|\.ott|\.ovh|\.pa|\.page|\.panasonic|\.paris|\.pars|\.partners|\.parts|\.party|\.passagens|\.pay|\.pccw|\.pe|\.pet|\.pf|\.pfizer|\.pg|\.ph|\.pharmacy|\.phd|\.philips|\.phone|\.photo|\.photography|\.photos|\.physio|\.piaget|\.pics|\.pictet|\.pictures|\.pid|\.pin|\.ping|\.pink|\.pioneer|\.pizza|\.pk|\.pl|\.place|\.play|\.playstation|\.plumbing|\.plus|\.pm|\.pn|\.pnc|\.pohl|\.poker|\.politie|\.porn|\.post|\.pr|\.pramerica|\.praxi|\.press|\.prime|\.pro|\.prod|\.productions|\.prof|\.progressive|\.promo|\.properties|\.property|\.protection|\.pru|\.prudential|\.ps|\.pt|\.pub|\.pw|\.pwc|\.py|\.qa|\.qpon|\.quebec|\.quest|\.qvc|\.racing|\.radio|\.raid|\.re|\.read|\.realestate|\.realtor|\.realty|\.recipes|\.red|\.redstone|\.redumbrella|\.rehab|\.reise|\.reisen|\.reit|\.reliance|\.ren|\.rent|\.rentals|\.repair|\.report|\.republican|\.rest|\.restaurant|\.review|\.reviews|\.rexroth|\.rich|\.richardli|\.ricoh|\.rightathome|\.ril|\.rio|\.rip|\.rmit|\.ro|\.rocher|\.rocks|\.rodeo|\.rogers|\.room|\.rs|\.rsvp|\.ru|\.rugby|\.ruhr|\.run|\.rw|\.rwe|\.ryukyu|\.sa|\.saarland|\.safe|\.safety|\.sakura|\.sale|\.salon|\.samsclub|\.samsung|\.sandvik|\.sandvikcoromant|\.sanofi|\.sap|\.sarl|\.sas|\.save|\.saxo|\.sb|\.sbi|\.sbs|\.sc|\.sca|\.scb|\.schaeffler|\.schmidt|\.scholarships|\.school|\.schule|\.schwarz|\.science|\.scjohnson|\.scor|\.scot|\.sd|\.se|\.search|\.seat|\.secure|\.security|\.seek|\.select|\.sener|\.services|\.ses|\.seven|\.sew|\.sex|\.sexy|\.sfr|\.sg|\.sh|\.shangrila|\.sharp|\.shaw|\.shell|\.shia|\.shiksha|\.shoes|\.shop|\.shopping|\.shouji|\.show|\.showtime|\.shriram|\.si|\.silk|\.sina|\.singles|\.site|\.sj|\.sk|\.ski|\.skin|\.sky|\.skype|\.sl|\.sling|\.sm|\.smart|\.smile|\.sn|\.sncf|\.so|\.soccer|\.social|\.softbank|\.software|\.sohu|\.solar|\.solutions|\.song|\.sony|\.soy|\.space|\.sport|\.spot|\.spreadbetting|\.sr|\.srl|\.srt|\.ss|\.st|\.stada|\.staples|\.star|\.starhub|\.statebank|\.statefarm|\.stc|\.stcgroup|\.stockholm|\.storage|\.store|\.stream|\.studio|\.study|\.style|\.su|\.sucks|\.supplies|\.supply|\.support|\.surf|\.surgery|\.suzuki|\.sv|\.swatch|\.swiftcover|\.swiss|\.sx|\.sy|\.sydney|\.symantec|\.systems|\.sz|\.tab|\.taipei|\.talk|\.taobao|\.target|\.tatamotors|\.tatar|\.tattoo|\.tax|\.taxi|\.tc|\.tci|\.td|\.tdk|\.team|\.tech|\.technology|\.tel|\.telefonica|\.temasek|\.tennis|\.teva|\.tf|\.tg|\.th|\.thd|\.theater|\.theatre|\.tiaa|\.tickets|\.tienda|\.tiffany|\.tips|\.tires|\.tirol|\.tj|\.tjmaxx|\.tjx|\.tk|\.tkmaxx|\.tl|\.tm|\.tmall|\.tn|\.to|\.today|\.tokyo|\.tools|\.top|\.toray|\.toshiba|\.total|\.tours|\.town|\.toyota|\.toys|\.tr|\.trade|\.trading|\.training|\.travel|\.travelchannel|\.travelers|\.travelersinsurance|\.trust|\.trv|\.tt|\.tube|\.tui|\.tunes|\.tushu|\.tv|\.tvs|\.tw|\.tz|\.ua|\.ubank|\.ubs|\.uconnect|\.ug|\.uk|\.unicom|\.university|\.uno|\.uol|\.ups|\.us|\.uy|\.uz|\.va|\.vacations|\.vana|\.vanguard|\.vc|\.ve|\.vegas|\.ventures|\.verisign|\.versicherung|\.vet|\.vg|\.vi|\.viajes|\.video|\.vig|\.viking|\.villas|\.vin|\.vip|\.virgin|\.visa|\.vision|\.vistaprint|\.viva|\.vivo|\.vlaanderen|\.vn|\.vodka|\.volkswagen|\.volvo|\.vote|\.voting|\.voto|\.voyage|\.vu|\.vuelos|\.wales|\.walmart|\.walter|\.wang|\.wanggou|\.warman|\.watch|\.watches|\.weather|\.weatherchannel|\.webcam|\.weber|\.website|\.wed|\.wedding|\.weibo|\.weir|\.wf|\.whoswho|\.wien|\.wiki|\.williamhill|\.win|\.windows|\.wine|\.winners|\.wme|\.wolterskluwer|\.woodside|\.work|\.works|\.world|\.wow|\.ws|\.wtc|\.wtf|\.xbox|\.xerox|\.xfinity|\.xihuan|\.xin|\.xxx|\.xyz|\.yachts|\.yahoo|\.yamaxun|\.yandex|\.ye|\.yodobashi|\.yoga|\.yokohama|\.you|\.youtube|\.yt|\.yun|\.za|\.zappos|\.zara|\.zero|\.zip|\.zm|\.zone|\.zuerich|\.zw)(\/.*)?$/i;
    return domainRegex;
  }

};

var formEvents = {

  handleShrinkEvent: function(event) {
    event.preventDefault();
    $('#urlError').hide();
    $('#fullUrlInput').removeClass('is-invalid');
    var fullUrlVal = $('#fullUrlInput').val();

    if (fullUrlVal.length === 0) {
      $('#urlError').html('Please enter a URL you want to shorten').show();
      $('#fullUrlInput').addClass('is-invalid');
      return;
    }
    if (fullUrlVal.length < 4) {
      $('#urlError').html('Please enter a valid URL you want to shorten').show();
      $('#fullUrlInput').addClass('is-invalid');
      return;
    }
    if (fullUrlVal.length >= 2000) {
      $('#urlError').html('Your link exceeds the 2000 character limit.').show();
      $('#fullUrlInput').addClass('is-invalid');
      return;
    }
    if (!fullUrlVal.startsWith("https://") && !fullUrlVal.startsWith("http://") && !fullUrlVal.startsWith("mailto:") && !fullUrlVal.startsWith("tel:")) {
      // check if the user just left off the http:// from their URL and add it if so
      if (shrinklyUtil.getDomainRegex().test(fullUrlVal)) {
        fullUrlVal = "http://" + fullUrlVal;
      } else {
        $('#urlError').html('Please use only the supported url schemes. (http, https, mailto, or tel)').show();
        $('#fullUrlInput').addClass('is-invalid');
        $("#fullUrlInput").effect("shake");
        return;
      }
    }

    // check for custom alias
    var customAliasVal = '';
    if ($('#customAlias').length) {
      $('#customAliasError').hide();
      $('#customAlias').removeClass('is-invalid');
      customAliasVal = $('#customAlias').val();
      if (customAliasVal.length > 0 && customAliasVal.length < 3 ) {
        $('#customAliasError').html('Please select an alias longer than 2 characters').show();
        $('#customAlias').addClass('is-invalid');
        return;
      }
      if (customAliasVal.length > 40) {
        $('#customAliasError').html('Please select an alias shorter than 40 characters').show();
        $('#customAlias').addClass('is-invalid');
        return;
      }
    }

    // check for link expiration
    var expireLink = false;
    var expiryDateTime = new Date();
    if ($("#toggleExpiration").length) {
      $('#customExpiryError').hide();
      // and is checked
      if ($("#toggleExpiration")[0].checked) {
        var expiryDateStr = $("#expirationDate").val();
        var d = new Date(expiryDateStr);
        var curD = new Date();
        if (d.getFullYear() < curD.getFullYear() || d.getMonth() < curD.getMonth() || d.getDate() < curD.getDate()) {
          $('#customExpiryError').html('Your expiry date must be set to a future date.').show();
          $('#dateOptionsCollapsable').effect('shake');
          return;
        }
        var expiryTimeStr = $("#expirationTime").val();
        var expiryDateTime = new Date(expiryDateStr + ' ' + expiryTimeStr);

        if (expiryDateTime <= curD) {
          $('#customExpiryError').html('Your expiry date must be set to a future date.').show();
          return;
        } else {
          expireLink = true;
        }
      }
    }

    $.ajax({
      type: "POST",
      url: "/shrink",
      data: JSON.stringify({ url: fullUrlVal, customAlias: customAliasVal, expireLink: expireLink, expiryDateTime: expiryDateTime.toUTCString() }),
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      success: function(data) {
        if(data.shortUrl) {
          var uniqueId = _.uniqueId('form') + _.random(1, 999).toString();
          var source = document.getElementById("newShortUrlTemplate").innerHTML;
          var template = Handlebars.compile(source);
          var analytics = data.clicks + ' clicks';
          var context = { shortUrl: data.shortUrl, fullUrl: shrinklyUtil.getTruncatedUrl(data.url),
            shortClicks: analytics, shortenedKey: data.shortenedKey, id: uniqueId };

          $('#newShortUrlBlock').prepend(template(context));

          // if user table exists add new url to it
          if ($('#userShortUrlTable').length) {
            var tableUniqueId = _.uniqueId('table');
            var sourceHtml = document.getElementById("newShortUrlTableTemplate").innerHTML;
            var now = new Date();
            var options = {  year: 'numeric', month: 'short', day: 'numeric', hour12: true, hour: 'numeric', minute: 'numeric' };
            var dateAdded = new Intl.DateTimeFormat('en-US', options).format(now);
            var temp = Handlebars.compile(sourceHtml);
            var tableContext = { shortUrl: data.shortUrl, fullUrl: data.url, shortClicks: data.clicks,
              shortCode: data.shortenedKey, dateAdded: dateAdded, id: tableUniqueId };

            $('#userShortUrlTable tbody').prepend(temp(tableContext));

            // setup table functionality for new short url
            var clipboard = new ClipboardJS('#copybtn' + tableUniqueId);
            $('#copybtn' + tableUniqueId).tooltip({ trigger: 'manual' });
            $('#copybtn' + tableUniqueId).bind( "click", function() {
              $('#copybtn' + tableUniqueId).tooltip('show');
              setTimeout(function(){
                $('#copybtn' + tableUniqueId).tooltip('hide');
              }, 1200);
            });

            var qrcode = new QRCode(document.getElementById("qrcode" + tableUniqueId), {
              width : 100,
              height : 100
            });
            var dataUrl = location.protocol + '//' + location.hostname + '/' + data.shortenedKey;
            try { qrcode.makeCode(dataUrl);} catch (e) { }

          }

          // setup the copy to clipboard feature
          var clipboardTable = new ClipboardJS('#' + uniqueId + 'btn');
          $('#' + uniqueId + 'btn').tooltip({ trigger: 'manual' });
          $('#' + uniqueId + 'btn').bind( "click", function() {
            $('#' + uniqueId + 'btn').tooltip('show');
            setTimeout(function(){
              $('#' + uniqueId + 'btn').tooltip('hide');
            }, 1200);
          });

          var sucEle = $('#shortenSuccess');
          var isVisible = sucEle.is(':visible');

          if (isVisible) {
            $('#newShortUrl').effect("highlight");
          } else {
            $('#shortenSuccess').show('drop', { direction: 'up'});
          }
          $('#fullUrlInput').val('');
        } else {
          $('#urlError').html('Server failure, please try again.').show();
        }

        },
      error: function(jqXHR, textStatus, errorThrown) {
        var errorMessage = jqXHR.responseJSON.message;
        var field = jqXHR.responseJSON.field;

        if (field === 'url') {
          $("#urlError").show().html(errorMessage);
          $('#fullUrlInput').addClass('is-invalid');
        }
        if (field === 'customAlias') {
          $('#customAliasError').html(errorMessage).show();
          $('#customAlias').addClass('is-invalid');
        }
        if (field === 'customExpiry') {
          $('#customExpiryError').html(errorMessage).show();
        }

      }
    });

  }

};

$(document).ready(function () {

  // home message modal popup logic
  if ($('#homeMessageModal').length) {
    var message = sessionStorage.getItem('message');
    if (message) {
      $('#homeMessage').html(message);
      $('#homeMessageModal').modal();
      sessionStorage.removeItem('message');
    }
    var len = $('#homeMessage').html().length;
    if (len > 0) {
      $('#homeMessageModal').modal();
    }

    if ($('#homeMessageParam').length) {
      $('#homeMessageModal').modal();

      // get rid of the query parameter on modal close so it won't trigger on reloads
      $('#homeMessageModal').on('hidden.bs.modal', function () {
        window.location = window.location.pathname;
      });
    }
    if ($('#homeMessagePendingReset').length) {
      $('#homeMessageTitle').html('Reset Pending');
      $('#homeMessageModal').modal();
      $('#homeMessageModal').on('hidden.bs.modal', function () {
        window.location.href = '/perform_logout';
      });
    }
  }

  var prevTitle = '';
  // add random subtitles
  var titleIntervalId = setInterval(function(){
    if ($("#shrinkSubtitle").length) {
      var randomTitle = shrinklyUtil.getRandomSubtitle();
      // dont cycle repeats
      while (prevTitle === randomTitle) {
        randomTitle = shrinklyUtil.getRandomSubtitle();
      }
      prevTitle = randomTitle;
      $( "#shrinkSubtitle" ).hide().html(randomTitle).show( "fade" );
    }
  }, 3200);

  // New Short URL form logic
  if ($('#shrinkForm').length) {

    // set default expiry date to current date/time
    var d = new Date();
    var optionsDate = { year: 'numeric', month: '2-digit', day: '2-digit' };
    var optionsTime = { hourCycle: 'h24', hour: '2-digit', minute: '2-digit' };

    $('#expirationDate').val(d.toLocaleDateString('en-us', optionsDate));
    $('#expirationTime').val(d.toLocaleTimeString('en-gb', optionsTime));

    $('#expirationDate').datepicker();

    $(document).ajaxStart( function() {
      $("#shrinkFormSubmitBtn").prop('disabled', true);
    }).ajaxStop( function() {
      $("#shrinkFormSubmitBtn").delay(300).prop('disabled', false);
    });

    $('#shrinkForm').submit(function(event) {
      formEvents.handleShrinkEvent(event);
    });

    $('#advancedOptions').change(function() {
      if (this.checked) {
        $('#advancedOptionsCollapsable').collapse('show');
        $('#advancedOptionsLabel').text("Hide Advanced Options");

      } else {
        $('#advancedOptionsCollapsable').collapse('hide');
        $('#advancedOptionsLabel').text("Show Advanced Options");
      }
    });

    $('#toggleExpiration').change(function() {
      if (this.checked) {
        $('#dateOptionsCollapsable').collapse('show');
        $('#timeOptionsCollapsable').collapse('show');

      } else {
        $('#dateOptionsCollapsable').collapse('hide');
        $('#timeOptionsCollapsable').collapse('hide');
      }
    });

  }

});