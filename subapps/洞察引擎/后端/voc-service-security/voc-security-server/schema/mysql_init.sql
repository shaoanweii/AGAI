INSERT INTO voc.sys_apps
(id, app_id, urls, note)
VALUES('1', 'voc', NULL, '');
INSERT INTO voc.sys_apps
(id, app_id, urls, note)
VALUES('2', 'modeltraining', 'http://www.baidu.com', NULL);
INSERT INTO voc.sys_apps
(id, app_id, urls, note)
VALUES('3', 'labelstud', NULL, NULL);
INSERT INTO voc.sys_apps
(id, app_id, urls, note)
VALUES('4', 'insights', NULL, NULL);
INSERT INTO voc.sys_apps
(id, app_id, urls, note)
VALUES('5', 'template', NULL, NULL);
INSERT INTO voc.sys_apps
(id, app_id, urls, note)
VALUES('6', 'insights', NULL, NULL);
INSERT INTO voc.sys_apps
(id, app_id, urls, note)
VALUES('7', 'template', NULL, NULL);



INSERT INTO voc.sys_users
(id, username, phone, firstname, lastname, email, create_time, update_time, operator, labelstud_token, non_locked, enabled, expire_date, start_expire_date, client_id)
VALUES('1', 'admin', NULL, 'admin', 'admin', NULL, '2024-02-20 13:11:57', '2024-02-20 13:11:57', '', NULL, 1, 1, '2099-12-30 00:00:00', '2000-01-01 00:00:00', NULL);

-- admin/Passw0rd@!
INSERT INTO voc.sys_credentials
(id, user_id, credential, app_id, identifier, identity_type, create_time, update_time, operator, non_locked, enabled, expire_date, start_expire_date, non_expired)
VALUES('1', '1', 'D6jpanSeFpjxFIvyMjydwdYweqfTCA5IWIptLHnfafzj2L4DutPooDHJFTEBv8K7', 'insights', 'admin', 'base', '2024-02-20 13:12:10', '2024-02-20 13:12:10', '13a65982eab9716202fd33442dc10178', 1, 1, '2099-12-30 00:00:00', '2000-01-01 00:00:00', 1);
INSERT INTO voc.sys_credentials
(id, user_id, credential, app_id, identifier, identity_type, create_time, update_time, operator, non_locked, enabled, expire_date, start_expire_date, non_expired)
VALUES('2', '1', 'D6jpanSeFpjxFIvyMjydwdYweqfTCA5IWIptLHnfafzj2L4DutPooDHJFTEBv8K7', 'template', 'admin', 'base', '2024-02-20 13:11:57', '2024-02-20 13:11:57', 'b6c4b77936a05f969e913de3927b2fb2', 1, 1, '2099-12-30 00:00:00', '2000-01-01 00:00:00', 1);