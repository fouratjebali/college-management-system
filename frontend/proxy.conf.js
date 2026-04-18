const fs = require('node:fs');

const isDocker = fs.existsSync('/.dockerenv');
const target = process.env.API_PROXY_TARGET || (isDocker ? 'http://backend:8080' : 'http://localhost:8080');

console.log(`[proxy] API target: ${target}`);

module.exports = {
  '/api': {
    target,
    secure: false,
    changeOrigin: true,
    logLevel: 'debug',
  },
};
