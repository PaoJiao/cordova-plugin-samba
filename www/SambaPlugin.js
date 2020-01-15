const exec = require('cordova/exec')
module.exports = {

  auth(username, password, success, error) {
    exec(success, error, 'SambaPlugin', 'auth', [ username, password ])
  },

  listFiles(path, success, error) {
    exec(success, error, 'SambaPlugin', 'listFiles', [ path ])
  },

  readAsText(path, success, error) {
    exec(success, error, 'SambaPlugin', 'readAsText', [ path ])
  },

  readAsByteArray(path, success, error) {
    exec(success, error, 'SambaPlugin', 'readAsByteArray', [ path ])
  },

  openImage(path, success, error) {
    exec(success, error, 'SambaPlugin', 'openImage', [ path ])
  },

  openMedia(path, success, error) {
    exec(success, error, 'SambaPlugin', 'openMedia', [ path ])
  },

  openFile(path, success, error) {
    exec(success, error, 'SambaPlugin', 'openFile', [ path ])
  },

  upload(localPath, smbPath, success, error) {
    exec(success, error, 'SambaPlugin', 'upload', [ localPath, smbPath ])
  },

  mkfile(path, success, error) {
    exec(success, error, 'SambaPlugin', 'mkfile', [ path ])
  },

  mkdir(path, success, error) {
    exec(success, error, 'SambaPlugin', 'mkdir', [ path ])
  },

  delete(path, success, error) {
    exec(success, error, 'SambaPlugin', 'delete', [ path ])
  },

  wakeOnLan(mac, success, error) {
    exec(success, error, 'SambaPlugin', 'wakeOnLan', [ mac ])
  }

}
