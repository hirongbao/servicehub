import os

filepath = 'C:/Data/Programs/projects/backend/servicehub/servicehub-admin/src/main/resources/mapper/HttpRequestLogMapper.xml'
with open(filepath, 'rb') as f:
    content = f.read()

if content.startswith(b'\xef\xbb\xbf'):
    content = content[3:]

with open(filepath, 'wb') as f:
    f.write(content)
print("BOM removed")
