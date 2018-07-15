'use strict'
// firebase deploy시에 FCM, RealTimeDatabase가 모두 설정되어있어서
// 데이터베이스 Rule이 바뀜. 현재로써는, read가 true로 되어있어야 푸시메시지를 통해 들어간 액티비티에 데이터값이 넘어옴.
// true상태는 public 상태이므로 보안에 안좋음.
const functions  = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();
 
exports.sendNotification = functions.database.ref('/notification/{user_id}/{notification_id}').onWrite((change,context) =>{
    const user_id = context.params.user_id;
    const notification_id = context.params.notification_id;

    console.log('We have a notification from : ' , user_id);

    if(!change.after.val()){
        return console.log('A Notification has been deleted from database : ', notification_id);
    }

    const fromUser = admin.database().ref(`/notification/${user_id}/${notification_id}`).once('value');
    // DB참조 경로가 string변수 값일경우 작은따옴표(')가아닌 물결키(`)로 경로 지정해야함.
    // DB참조 경로의 첫 노드가 대문자일 경우 읽어오지 못함.
    
    return fromUser.then(fromUserResult=>{

        const from_user_id = fromUserResult.val().from;

        console. log('You have new notification from', from_user_id);

        const userQuery = admin.database().ref(`/users/${from_user_id}/name`).once('value');    
        const deviceToken = admin.database().ref(`/users/${user_id}/deviceToken`).once('value');
        const userImage = admin.database().ref(`/users/${from_user_id}/photo`).once('value');

    
        return Promise.all([userQuery, deviceToken,userImage]).then(result => {
    
         const userName = result[0].val();
         const token_id = result[1].val();
         const userPhoto = result[2].val();
         

         var payload = {

            data: {   
                tag : 'contact',      
                from_user_id : from_user_id,
                title : "친구 요청",
                body : `${userName} 님이 친구요청을 보냈습니다.`,
                userPhoto : `${userPhoto}`,
                click_action : "com.example.home.mytalk_TARGET_NOTIFICATION" 
                }
            };

                return admin.messaging().sendToDevice(token_id , payload).then(response =>{
                    return console.log('This was the Notification');
                });
        
            });
           
        });

    });


    exports.chatNotification = functions.database.ref('/oneToOneMessage/{to_user_id}/{from_user_id}').onWrite((snap,context) =>{
        const to_user_id = context.params.to_user_id;
        const from_user_id = context.params.from_user_id;

        var lastChild;
        snap.after.forEach(function(child){
            lastChild = child;
        });
        //1:1채팅 메시지 노드의 마지막 노드만 가져옴
        
        const fromUsers = admin.database().ref(`/oneToOneMessage/${to_user_id}/${from_user_id}`).once('value');
        
        //console.log('lastChild : ',lastChild.val()); 
        return fromUsers.then(fromUserResult=>{

            //const from_user_id = fromUserResult.val().from;
            const name = lastChild.child('name').val();
            const key = lastChild.child('from').val();
            var deviceTokens = admin.database().ref(`/users/${to_user_id}/deviceToken`).once('value'); //메시지 받는 사람 토큰
            var deviceTokenSender = admin.database().ref(`/users/${key}/deviceToken`).once('value'); //메시지 보낸 사람 토큰
            const userPhoto = lastChild.child('photo').val();
            const lastmessage = lastChild.child('text').val();
                
            return Promise.all([name, deviceTokens ,userPhoto ,lastmessage, deviceTokenSender,from_user_id]).then(result => {
    
                const user_name = result[0];
                const Token =  result[1].val();
                const user_photo = result[2];
                const message = result[3];
                const TokenSender = result[4].val();
                const one_room = result[5];

                console.log(user_name,Token,user_photo,message,one_room);

                var payload = {
                    data: {   
                        tag : 'one', 
                        room : one_room,  
                        tokenId : Token,
                        tokenIdSender : TokenSender,  //보낸사람, 받는사람 토큰값을 비교해서 보낸사람의 기기에는 푸시메시지 X
                        name : user_name,
                        message : message,
                        userPhotoImage : `${user_photo}`,
                        click_action_message : "com.example.home.mytalk_Message_NOTIFICATION" 
                        }
                };

                return admin.messaging().sendToDevice(Token , payload).then(response =>{
                        return console.log('데이터 필드값 :',user_name,Token,user_photo,message,one_room);
                });
            });
        });
    });

    exports.groupChatNotification = functions.database.ref('/groupMessage/{room}').onWrite((snap,context) =>{
    
        var lastChild;
        snap.after.forEach(function(child){
            lastChild = child;
        }); 

       
        const room = context.params.room; //그룹방 이름
        const name = lastChild.child('name').val();
        const photoUrl = lastChild.child('photo').val();
        const key = lastChild.child('key').val();
        const message = lastChild.child('text').val();
        const senderToken = admin.database().ref(`/users/${key}/deviceToken`).once('value');
        const joinUser = admin.database().ref(`/friendChatRoom/${key}/${room}/joinUserKey`).once('value');

        return Promise.all([name, photoUrl ,key ,message, joinUser,senderToken,room]).then(result => {
            const user_name = result[0];
            const user_photo = result[1];
            const user_key =  result[2];
            const user_message = result[3];
            var joinUserKey = result[4].val(); //방 참가자들의 키값.
            var sender_Token = result[5].val();
            const group_room = result[6];
            let array = [];

            joinUserKey.forEach(function(userKey, index){
                   
                    admin.database().ref(`/users/${userKey}/deviceToken`).once('value').then(device_token =>{
                    
                        return Promise.all([user_name, device_token.val() ,user_photo ,user_message, sender_Token,group_room, user_key]).then(result => {
                            array.push(userKey);
                            const group_user_name = result[0];
                            const group_user_token =  result[1];
                            const group_user_photo = result[2];
                            const group_message = result[3];
                            const group_sender_Token = result[4];
                            const group_room_name = result[5];
                            const group_sender_key = result[6];

                            var payload = {
                                data: {   
                                    tag : 'group',  
                                    gsenderKey : group_sender_key,
                                    groom : group_room_name, //그룹채팅방 이름
                                    gtokenIdSender : group_sender_Token, //메시지 보낸 사람 토큰
                                    gtokenId : group_user_token,  // 메시지 받는 사람들의 토큰 ---> 앱에서 두 값을 받아 비교해서 같으면 그 기기에는 노티 안띄워짐
                                    gname : group_user_name,
                                    gmessage : group_message,
                                    guserPhotoImage : `${group_user_photo}`,
                                    gclick_action_message : "com.example.home.mytalk_Message_NOTIFICATION" 
                                    }
                            };
            
                            return admin.messaging().sendToDevice(group_user_token , payload).then(response =>{
                                return console.log('보낸사람토큰:',group_sender_Token,'토큰값:',group_user_token,'이름:',group_user_name,'사진:',group_user_photo,'메시지:',group_message);

                            });
                        });
                    }).catch(error =>{
                        console.error(error);
                    });              

            });
            
            //joinUserKey에는 새 메시지가 추가된 그룹채팅방에 참가한 유저들의 키값이 있음.
            //키값을 통해 참가 유저들의 user노드 아래의 deviceToken값을 가져와서 반복문을 통해
            //sendToDevice로 각 유저들에게 보냄
            return  console.log('JoinUserKey : ',room+'/'+key+'/'+array);
        });
       
    });