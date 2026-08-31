-- V6：微信、QQ 社交名片二维码替换为真实 COS 图片
UPDATE site_social SET qr_code_url = 'https://hirongbao-1321185798.cos.ap-shanghai.myqcloud.com/images/df64c79b-1c3e-4fdc-bd1b-0bb26e074820.png' WHERE platform = '微信';
UPDATE site_social SET qr_code_url = 'https://hirongbao-1321185798.cos.ap-shanghai.myqcloud.com/images/9eeb855a-c214-43bc-8d56-ad44720706fc.png' WHERE platform = 'QQ';
