import * as functions from 'firebase-functions'
import * as admin from 'firebase-admin'
admin.initializeApp()


export const sendGameRequest = functions.firestore.document('/partida/convites/{invitedUid}/{inviterUid}').onWrite(async (snap, context) => {
    const invitedUid = context.params.invitedUid;
    const inviterUid = context.params.inviterUid;

    const getInviterUserProfile = admin.firestore().collection('usuarios').doc(inviterUid).get();
    const getInvitedUserProfile = admin.firestore().collection('usuarios').doc(invitedUid).get();

    const results = await Promise.all([getInviterUserProfile, getInvitedUserProfile]);
    const inviterUserProfile = results[0];
    const invitedUserProfile = results[1];


    // Detalhes da notificação
    const payload = {
        notification: {
            title: 'Novo convite de jogo!',
            body: inviterUserProfile.get('nome') + ':' + inviterUid
        }
    };


    console.log("Nome do usuário convidado: " + invitedUserProfile.get('nome'))
    console.log("Token do usuário convidado: " + invitedUserProfile.get('registrationToken'))

    return admin.messaging().sendToDevice(invitedUserProfile.get('registrationToken'), payload);
});

// // Start writing Firebase Functions
// // https://firebase.google.com/docs/functions/typescript
//
// export const helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
