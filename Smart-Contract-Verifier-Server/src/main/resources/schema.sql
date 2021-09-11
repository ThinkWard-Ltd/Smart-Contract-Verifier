drop table if exists public.user cascade;
create table public.user (public_walletid varchar(255) not null, alias varchar(255), nonce int8 not null, primary key (public_walletid));
drop table if exists agreements cascade;
create table agreements (contractid uuid not null, agreement_description varchar(255), agreement_imageurl varchar(255), agreement_title varchar(255), created_date timestamp, duration_conditionuuid uuid, moved_to_block_chain boolean not null, paying_party varchar(255), payment_conditionuuid uuid, sealed_date timestamp, blockchainid numeric(19, 2), primary key (contractid));
drop table if exists conditions cascade;
create table conditions (conditionid uuid not null, condition_description varchar(255), condition_status bytea, condition_title varchar(255), proposal_date timestamp, contract_contractid uuid, proposing_user_public_walletid varchar(255), primary key (conditionid));
drop table if exists contact_list cascade;
create table contact_list (contact_listid uuid not null, contact_list_name varchar(255), owner_public_walletid varchar(255), primary key (contact_listid));
drop table if exists contact_list_profile cascade;
create table contact_list_profile (profileid uuid not null, contact_alias varchar(255), contact_list_contact_listid uuid, user_public_walletid varchar(255), primary key (profileid));
drop table if exists evidence cascade;
create table evidence (evidence_hash varchar(255) not null, evidence_type bytea, removed boolean not null, contract_contractid uuid, user_public_walletid varchar(255), primary key (evidence_hash));
drop table if exists judges cascade;
create table judges (judge_agreementid uuid not null, agreement_contractid uuid, judge_public_walletid varchar(255), primary key (judge_agreementid));
drop table if exists linked_evidence cascade;
create table linked_evidence (evidenceid uuid not null, evidence_url varchar(255), evidence_evidence_hash varchar(255), primary key (evidenceid));
drop table if exists messages cascade;
create table messages (messageid uuid not null, message varchar(255), send_date timestamp, agreements_contractid uuid, sender_public_walletid varchar(255), primary key (messageid));
drop table if exists message_status cascade;
create table message_status (message_statusid uuid not null, read_date timestamp, message_messageid uuid, recipient_public_walletid varchar(255), primary key (message_statusid));
drop table if exists uploaded_evidence cascade;
create table uploaded_evidence (evidenceid uuid not null, file_mime_type varchar(255), filename varchar(255), original_filename varchar(255), evidence_evidence_hash varchar(255), primary key (evidenceid));
drop table if exists user_agreement cascade;
create table user_agreement (public_walletid varchar(255) not null, contractid uuid not null, primary key (public_walletid, contractid));
alter table conditions add constraint FKpr7yy9rgi6w2tupy14o3lpwur foreign key (contract_contractid) references agreements;
alter table conditions add constraint FKf5jwtgem41ys5dqxul3mges72 foreign key (proposing_user_public_walletid) references public.user;
alter table contact_list add constraint FK5d2wo4ku79ty5wy0sp91wmfne foreign key (owner_public_walletid) references public.user;
alter table contact_list_profile add constraint FK1i7nmkgr287spuk0nbtuoq41a foreign key (contact_list_contact_listid) references contact_list;
alter table contact_list_profile add constraint FKoexhgbgvmx752nsvk805144ov foreign key (user_public_walletid) references public.user;
alter table evidence add constraint FKlpioqqatotxodcjc4rw9es0oc foreign key (contract_contractid) references agreements;
alter table evidence add constraint FK98ib2d8e7lq8d96jwrup8u8qf foreign key (user_public_walletid) references public.user;
alter table judges add constraint FK45ou5j98s4pvyp2aoid0fxmt1 foreign key (agreement_contractid) references agreements;
alter table judges add constraint FKrs0xin46m7dds86a4uxwafqnf foreign key (judge_public_walletid) references public.user;
alter table linked_evidence add constraint FKc3qd7g9wvw4gaxf16fuk3b47h foreign key (evidence_evidence_hash) references evidence;
alter table messages add constraint FK2uxxv8vyqrtmhpge7hoqek05d foreign key (agreements_contractid) references agreements;
alter table messages add constraint FKpogdllq7macivuimwivkatgaq foreign key (sender_public_walletid) references public.user;
alter table message_status add constraint FKn8xwifbqv0xwvi8byf2lffn7u foreign key (message_messageid) references messages;
alter table message_status add constraint FKh4xpvtmoew5cvg48aba8uqhyw foreign key (recipient_public_walletid) references public.user;
alter table uploaded_evidence add constraint FKi1tejij1ntihpua2py82nddoh foreign key (evidence_evidence_hash) references evidence;
alter table user_agreement add constraint FKqmikxjshk09mrgx5fp6utjcmb foreign key (contractid) references agreements;
alter table user_agreement add constraint FKm2k4vt46unbq35f9jckoxk12s foreign key (public_walletid) references public.user;
