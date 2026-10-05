const http = require('http');
http.get('http://localhost:8080/api/hirongbaohub/posts/page?page=1&size=1', (res) => {
  let data = '';
  res.on('data', chunk => data += chunk);
  res.on('end', () => console.log(JSON.stringify(JSON.parse(data), null, 2)));
});
