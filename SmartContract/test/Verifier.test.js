const { assert } = require('chai')

const {giveJurorsCoins} = require("./helper.js")

const UnisonToken = artifacts.require("UnisonToken")
const Verifier = artifacts.require("Verifier")
const RandomSource = artifacts.require("Randomness/RandomSource")

const truffleAssert = require('truffle-assertions');
require('chai').use(require('chai-as-promised')).should()

contract('Verifier', (accounts) =>{
    // Unit tests for smart contract go here

    describe("Verifier unit tests", async () =>{

        var verifier

        before(async () =>{
            token = await UnisonToken.new()
            r = await RandomSource.new();
            verifier = await Verifier.new(token.address, r.address);

            needCoins = [];
            for(var i = 1; i<9; i++){
                needCoins.push(accounts[i]);
            }
            giveJurorsCoins(token, accounts[0], needCoins, 100000);
        })

        it("Can create agreement", async () =>{

            verifier.createAgreement(accounts[1], 0, "do nothing with this agreement", "");

            var agree = await verifier.getAgreement(0)

            assert.equal(agree.party1, accounts[0])
            assert.equal(agree.party2, accounts[1])
            assert.equal(agree.state, 1)

        })

        it("Can't accept someone else's agreement", async () =>{
            verifier.acceptAgreement(0, {from: accounts[2]})

            var agree = await verifier.getAgreement(0)
            assert.equal(agree.state, 1)

        })

        it("Can accept agreement", async () =>{
            verifier.acceptAgreement(0, {from: accounts[1]})

            var agree = await verifier.getAgreement(0)
            assert.equal(agree.state, 3)

        })

        it("Can pay platform fee", async () =>{
            var agree = await verifier.getAgreement(0);
            var mustPay = agree.platformFee

            token.approve(verifier.address, mustPay);
            verifier.payPlatformFee(0);

            var agree = await verifier.getAgreement(0);
            assert(agree.feePaid == agree.platformFee);

        })

        it("Vote on agreement", async()=>{
            verifier.voteResolution(0, 2, {from: accounts[0]});
            verifier.voteResolution(0, 2, {from: accounts[1]});

            var agree = await verifier.getAgreement(0);

            assert.equal(agree.party1Vote, 2)
            assert.equal(agree.party2Vote, 2)
        })

        it("isJuror function", async() =>{
            var isJ = await verifier.isJuror(accounts[3]);
            assert(isJ == false, "non-juror seen as juror");

            token.approve(verifier.address, 10000, {from: accounts[3]});
            verifier.addJuror({from: accounts[3]});

            isJ = await verifier.isJuror(accounts[3]);
            assert(isJ == true, "juror seen as non-juror");

            verifier.removeJuror({from: accounts[3]});
            
            isJ = await verifier.isJuror(accounts[3]);
            assert(isJ == false, "removed juror still seen as juror");

        })

        it("Add jurors ", async()=>{
            // Add enough potential members to jury
            for(var i=2; i<7; i++){
                token.approve(verifier.address, 10000, {from: accounts[i]});
                verifier.addJuror({from: accounts[i]});
            }
        })

        it("Create 2nd agreement ", async()=>{
            // Create new agreement
            verifier.createAgreement(accounts[1], 0, "For jury test", "");

            var amount = 100
            await token.approve(verifier.address, amount);
            await verifier.addPaymentConditions(1, [token.address], [amount]);

            verifier.acceptAgreement(1, {from: accounts[1]})

            var agree = await verifier.getAgreement(1);
            var mustPay = agree.platformFee;

            token.approve(verifier.address, mustPay);
            verifier.payPlatformFee(1);

        })

        it("Vote no on agreement ", async()=>{
            // console.log("Vote no");

            result = await verifier.voteResolution(1, 1, {from: accounts[0]});


            var agree = await verifier.getAgreement(1);
            var jury = await verifier.getJury(1);
            // console.log(agree);

            assert.equal(agree.party1Vote, 1, "incorrect vote in Agreement")
            assert.equal(jury.assigned, true, "Jury wasn't assigned");

            truffleAssert.eventEmitted(result, "JuryAssigned", (ev)=>{
                return ev.agreeID == 1
            });

        })


        it("Get jury", async()=>{
            var jury = await verifier.getJury(1);

            for(var i=0; i<jury.jurors.length; i++){
                var found = false;

                for(var j=2; j<7; j++){
                    //accounts 3 to 8 (included) are signed up as jurors
                    if(jury.jurors[i] == accounts[j]){
                        found = true;
                        break;
                    }
                }
                assert(found, "Invalid account on jury");

            }
        })

        it("Jury voting Yes", async()=>{
            var juryStart = await verifier.getJury(1);
            var vote = 2;

            agree = await verifier.getAgreement(1);
            var to = agree.payments[0].to;
            var balPre = await token.balanceOf(to);
            balPre = BigInt(balPre);

            // Each juror votes
            for(var i=0; i<juryStart.jurors.length; i++){
                await verifier.jurorVote(1, vote, {from : juryStart.jurors[i]});

            }

            // Check that all votes were recorded properly
            jury = await verifier.getJury(1);
            for(var i=0; i<juryStart.jurors.length; i++)
                assert(jury.votes[i] == vote, "Vote wasn't properly updated");


            agree = await verifier.getAgreement(1);
            // console.log(agree);
            // agree.state 9 means CLOSED
            assert(agree.state == 9, "Agreement wasn't closed on unanimous YES vote") 

            var balPost = await token.balanceOf(to);
            balPost = BigInt(balPost);

            assert(balPost - balPre == agree.payments[0].amount, "Agreement didn't pay out")


        })

    })

    describe("Verifier unit tests 2", async () =>{

        var verifier

        before(async () =>{
            token = await UnisonToken.new()
            r = await RandomSource.new();
            verifier = await Verifier.new(token.address, r.address);

            // Create agreement
            await verifier.createAgreement(accounts[1], 0, "do nothing with this agreement", "");
            await verifier.acceptAgreement(0, {from: accounts[1]})

            // Pay platofrm fee
            var agree = await verifier.getAgreement(0);
            var mustPay = agree.platformFee

            await token.approve(verifier.address, mustPay);
            await verifier.payPlatformFee(0);
        })

        it("Add payment condition", async()=>{
            var agree = await verifier.getAgreement(0);
            var numPaymentsAlready = agree.payments.length;

            var amount = 100
            await token.approve(verifier.address, amount);
            await verifier.addPaymentConditions(0, [token.address], [amount]);

            agree = await verifier.getAgreement(0);
            assert(agree.payments.length == numPaymentsAlready + 1);
        })

        it("Add multiple payment conditions", async()=>{
            var agree = await verifier.getAgreement(0);
            var numPaymentsAlready = agree.payments.length;

            var amount = 100
            await token.approve(verifier.address, amount*2);
            await verifier.addPaymentConditions(0, [token.address, token.address], [amount, amount]);

            agree = await verifier.getAgreement(0);
            assert(agree.payments.length == numPaymentsAlready + 2);
        })

        it("Need allowance for payment condition", async()=>{
            var agree = await verifier.getAgreement(0);
            var numPaymentsAlready = agree.payments.length;

            var amount = 100

            // This should throw an error
            try{
            await verifier.addPaymentConditions(0, [token.address], [amount]);
            }
            catch{}

            agree = await verifier.getAgreement(0);
            assert(agree.payments.length == numPaymentsAlready);
        })        

    })

    describe("Verifier unit tests 3", async () =>{

        var verifier

        before(async () =>{
            token = await UnisonToken.new()
            r = await RandomSource.new();
            verifier = await Verifier.new(token.address, r.address);

            // Create agreement
            await verifier.createAgreement(accounts[1], 0, "Will be used for jury testing", "");
        
            // Add payment condition
            var amount = 100
            await token.approve(verifier.address, amount);
            await verifier.addPaymentConditions(0, [token.address], [amount]);

            // Accept
            await verifier.acceptAgreement(0, {from: accounts[1]})

            // Pay platofrm fee
            var agree = await verifier.getAgreement(0);
            var mustPay = agree.platformFee

            await token.approve(verifier.address, mustPay);
            await verifier.payPlatformFee(0);

            // Prepare jurors
            needCoins = [];
            for(var i = 1; i<9; i++){
                needCoins.push(accounts[i]);
            }
            giveJurorsCoins(token, accounts[0], needCoins, 100000);

            for(var i=3; i<9; i++){
                token.approve(verifier.address, 10000, {from: accounts[i]});
                verifier.addJuror({from: accounts[i]});
            }

            // Contest agreement
            result = await verifier.voteResolution(0, 1, {from: accounts[1]});


        })

        it("Agreement is contested", async()=>{
            var jury = await verifier.getJury(0);
            assert(jury.jurors.length > 0, "Jury wasn't assigned");
        })

        it("Jury votes NO", async ()=>{
            var juryStart = await verifier.getJury(0);
            var vote = 1;

            agree = await verifier.getAgreement(0);
            var from = agree.payments[0].from;
            var balPre = await token.balanceOf(from);
            balPre = BigInt(balPre);
            
            // Each juror votes
            for(var i=0; i<juryStart.jurors.length; i++){
                await verifier.jurorVote(0, vote, {from : juryStart.jurors[i]});

            }

            // Check that all votes were recorded properly
            jury = await verifier.getJury(0);
            for(var i=0; i<juryStart.jurors.length; i++)
                assert(jury.votes[i] == vote, "Vote wasn't properly updated");


            agree = await verifier.getAgreement(0);
            // console.log(agree);
            // agree.state 9 means CLOSED
            assert(agree.state == 9, "Agreement wasn't closed on unanimous NO vote") 

            var balPost = await token.balanceOf(from);
            balPost = BigInt(balPost);

            assert(balPost - balPre == agree.payments[0].amount, "Agreement didn't refund")     
        })

    })

})