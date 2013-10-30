/*global $, jQuery, alert, console, document, setTimeout, angular*/
/**
 *
 * @authors Ted Shiu (tedshd@gmail.com)
 * @date    2013-10-27 17:08:54
 * @version $Id$
 */

$(function () {
    // static data
    var profile = {
            'facebook_id': '123456',
            'facebook_avatar': 'url',
            'facebook_name': 'user_name1',
            'win': 0,
            'lose': 0
        },
        question = {
            'question_lib': {
                'question': 'QQ',
                'answer': 'a_1',
                'options': {
                    'a_1': 'ans_1',
                    'a_2': 'ans_2',
                    'a_3': 'ans_3',
                    'a_4': 'ans_4'
                }
            }
        },
        fail = {
            status: 'fail',
            fail_ans: 0
        },
        ok = {
            status: 'ok',
            answer: 1
        };
    // static data end

    var n = 0,
        fail_id;

    function reciprocal() {
        var rec,
            count = 3;
        rec = setInterval(
            function () {
                // body...
                console.log('count: ' + count);
                $('.reciprocal').html(count);
                if (count === 0) {
                    // $('.reciprocal').html('');
                    // window.game.start();
                    clearInterval(rec);
                }
                count--;
            },
            1000
        );
    }

    // addPlayer
    function addPlayer(data) {
        console.log(data);
        var avatar,
            name,
            win,
            lose,
            f_id;

            f_id = data.facebook_id;
            avatar = data.facebook_avatar;
            name = data.facebook_name;
            win = data.win;
            lose = data.lose;
            $('.avatar:eq(' + n + ') img').attr('src', avatar).parent('.avatar').attr('id', f_id);
            $('.name:eq(' + n + ')').html(name);
            $('.rank:eq(' + n + ')').html(win + ' Win | ' + lose + ' Lose');
            if (n === 3) {
                setTimeout(function () {
                    finishAdd();
                }, 10000);
            }
            n++;
    }

    // finish add 4 player
    function finishAdd() {
        // body...
        $('.question_bg').removeClass('hide');
        $('#player').removeClass('hide');
        $('.ans_bg').removeClass('hide');
        $('.loading').addClass('hide');
    }

    // showQuestion and begin 3210 call start
    function showQuestion(data) {
        // reset UI
        $('.hexagonal_ans').removeClass('dark');
        $('.ans').removeClass('correct');
        $('.avatar').removeClass('dark');
        console.log(data);

        var n = 0,
            x,
            q,
            a_array;
        q = data.question_lib.question;
        a_array = data.question_lib.options;
        $('.question_area').html(q);
        for (x in a_array) {
            $('.ans:eq(' + n + ')').html(a_array[x]);
            n++;
        }
        reciprocal();
    }

    // selectAnswerer
    function selectAnswerer(fbid) {
        // body...
        console.log(fbid);
        $('.avatar').addClass('dark');
        $('#' + fbid + '').removeClass('dark');
        fail_id = fbid;
    }

    // result
    function result(data) {
        // body...
        if (data.status === 'ok') {
            // right
            // win UI
            $('.ans:eq(' + data.answer + ')').addClass('correct');
            // play music
        } else {
            // fail
            // update UI(fail user dark)
            $('.hexagonal_ans:eq(' + data.answer + ')').addClass('dark');
            $('.avatar').removeClass('dark');
            $('#' + fail_id + '').addClass('dark');
            reciprocal();
        }
    }

    /**
     * TEST Area
     * @return {[type]} [description]
     */
    addPlayer(profile);
    addPlayer(profile);
    addPlayer(profile);
    addPlayer(profile);
    selectAnswerer(123456);
    setTimeout(function () {
        showQuestion(question);
    }, 5000);
    setTimeout(function () {
        result(ok);
    }, 10000);

});



if (document.getElementById('player2')) {
    // get youtube api
    var tag = document.createElement('script'),
        firstScriptTag = document.getElementsByTagName('script')[0];
    tag.src = "https://www.youtube.com/iframe_api?client=181113669792.apps.googleusercontent.com&key=AIzaSyAqP-AGifepfLUe_XqZDbC75B3e4xYmVlk";
    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

    var playList = [],
        playCount = 0,
        player,
        onPlayerStateChange,
        value,
        key;

    var video_ID = {
        'a_0': 'f-FOf8RRq-U',
        'a_1': '7wvNwOPprBE',
        'a_2': 'LyMjaZE0GE8',
        'a_3': 'xWTiOqJqkk0',
        'a_4': 'F2uX6ByoW7A',
        'a_5': 'LWmVK8K2QhM',
        'a_6': 'mTSuiGubCHE'
    };

    // playList array
    for (key in video_ID) {
        value = video_ID[key];
        console.log(value);
        playList.push(value);
    }

    function playChannel() {
        // init player
        player = new YT.Player('player', {
            height: '350',
            width: '480',
            videoId: playList[0],
            playerVars: {
                rel: 1,
                autoplay: 0,
                disablekb: 0,
                showsearch: 0,
                showinfo: 0,
                controls: 0,
                wmode: 'opaque',
                hd: 1,
                iv_load_policy: 3,
                start: 60
            },
            events: {
                'onReady': onPlayerReady,
                'onStateChange': onPlayerStateChange
            }
        });

        // play video
        function onPlayerReady(event) {
            event.target.playVideo();
        }
    }

    // play list loop
    function onPlayerStateChange(event) {
        console.log(event);
        if (event.data === YT.PlayerState.PLAYING) {
            setTimeout(
                function () {
                    player.pauseVideo();
                },
                10000
            );
        }
        if (event.data === 0) {
            console.log('Next');
            playCount++;
            if (playCount > (playList.length - 1)) {
                playCount = 0;
            }
            player.loadVideoById(playList[playCount]);
            player.playVideo();
        }
    }

    function onYouTubeIframeAPIReady() {
        $(function () {
            console.log('jQuery');
            playChannel();
            $('button').on('click', function () {
                playList = ['DLFPqJedFuw', 'MmHkPXG4Td4'];
                // update play list
                // playList =
                player.loadVideoById(playList[0]);
                onPlayerStateChange();

            });

        });
    }
}