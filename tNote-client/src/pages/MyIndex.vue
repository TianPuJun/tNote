<template>
  <div class="hei">
    <el-container class="hei">
      <el-header style="text-align: right; font-size: 12px">
        <div class="el-sup">
          <h1>TZ笔记</h1>
        </div>
        <div class="el-title">
          <el-link icon="el-icon-folder-add" :underline="false" @click="open">新建文件夹</el-link>
          <el-dropdown>
            <el-link icon="el-icon-document-add" :underline="false">新建日记</el-link>
            <el-dropdown-menu slot="dropdown">
              <!-- <el-dropdown-item
                icon="el-icon-circle-plus-outline"
                @click.native="openEdit('html')"
              >新建富文本</el-dropdown-item> -->
              <el-dropdown-item
                icon="el-icon-circle-plus-outline"
                @click.native="openEdit('md')"
              >新建MarkDown</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>

        <div class="el-setter">
          <el-dropdown @command="handleCommand">
            <el-avatar src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"></el-avatar>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>

      <el-container class="hei">
        <el-aside width="200px">
          <el-menu :default-openeds="['1']" :unique-opened="false">
            <el-submenu index="1">
              <template slot="title">
                <i class="el-icon-s-promotion"></i>我的分类
              </template>
              <el-menu-item
                v-for="(item,index) in itemData "
                :key="index"
                :index="item.id.toString()"
                @click="getDiary(item)"
              >
                <i class="el-icon-folder"></i>
                {{item.title}}
              </el-menu-item>
            </el-submenu>

            <el-submenu index="2">
              <template slot="title">
                <i class="el-icon-setting"></i>我的设置
              </template>
              <el-menu-item index="2-1" @click="link = 'setter'">日记</el-menu-item>
              <!-- <el-menu-item
                v-for="(item,index) in itemData "
                :key="index"
                :index="item.id.toString()"
                @click="getDiary(item)"
              >
                <i class="el-icon-folder"></i>
                {{item.title}}
              </el-menu-item>-->
            </el-submenu>
          </el-menu>
        </el-aside>
        <el-container>
          <el-main style="padding: 0px;position:relative">
            <div v-if="showEdit" class="hei">
              <!-- <el-input v-model="form.name" placeholder="日记名称">
                <el-button slot="append" title="保存" icon="el-icon-check" @click="submit"></el-button>
              </el-input> -->
              <uEdit v-if="noteType === 'html'" class="hei" ref="ue" :value="edit.content"></uEdit>
              <mark-down
                v-if="noteType === 'md'"
                class="hei"
                @save="getEditorContent"
                ref="markdown"
                v-bind:mdContent="edit.content"
              ></mark-down>
              <el-dialog title="日记详情" :visible.sync="dialogFormVisible">
  <el-form :model="form">
    <el-form-item label="日记名称" :label-width="formLabelWidth">
      <el-input v-model="form.name" ></el-input>
    </el-form-item>
    <el-form-item label="日记分类" :label-width="formLabelWidth">
      <el-select v-model="form.item" placeholder="请选择类别" style="display: block;">
                      <el-option
                          v-for="(item,index) in itemData"
                          :key="index"
                          :label="item.title"
                          :value="item.id"
                        ></el-option>
      </el-select>
    </el-form-item>
  </el-form>
  <div slot="footer" class="dialog-footer">
    <el-button @click="dialogFormVisible = false">取 消</el-button>
    <el-button type="primary" @click="saveContent()">确 定</el-button>
  </div>
</el-dialog>
            </div>
            <div v-else>
              <show-diary
                v-if="link === 'show'"
                :diaryData="diaryData"
                :diaryTotal="diaryTotal"
                @handleDelete="handleDelete"
                @selectPageNum="selectPageNum"
              ></show-diary>
              <setter-content v-else-if="link === 'setter'"></setter-content>
            </div>
          </el-main>
          <el-footer>@TZ笔记(联系邮箱:3128379695@qq.com)</el-footer>
        </el-container>
      </el-container>
    </el-container>
  </div>
</template>
<script>
import { mapState, mapActions } from 'vuex'
// import uuid from 'uuid'
import MarkDown from '@/components/MarkDown'
import UEdit from '@/components/UE'
import ShowDiary from '@/pages/diary/ShowDiaryList'
import * as DiaryApi from '@/api/diary/Diary'
import * as Check from '@/utils/Check'
import { LogOut } from '@/api/user/login'
import SetterContent from '@/pages/diary/SetterContent'
import * as EncryptionApi from '@/api/diary/Encryption'

export default {
  name: 'index',
  components: {
    MarkDown,
    UEdit,
    ShowDiary,
    SetterContent
  },
  data () {
    return {
      itemData: [], // 分类数据
      diaryData: [], // 当前日记数据
      diaryTotal: 0, // 当前分类日记总条数
      ss: this.$store.state.diary.data,
      showEdit: false, // 显示编辑器
      edit: {
        content: ''
      }, // ue内容
      form: {
        id: null,
        name: '',
        item: ''
      }, // form表单内容
      dialogFormVisible: false, // 显示抽屉
      loading: false, // 加载
      formLabelWidth: '80px', // 抽屉label
      noteType: '',
      loginUser: JSON.parse(localStorage.getItem('userInfo')).realName,
      link: '', // 当前内容区域显示
      pageNum: 1, // 展示分页数据，第几页
      itemObject: null // 当前选中的分类
    }
  },
  computed: {
    ...mapState('diary', {
      count: state => state.count,
      showContent: state => state.showContent
    })
  },
  watch: {
    showContent (data) {
      console.log('监听store', data)
      this.showEditContent(data)
    }
  },
  methods: {
    ...mapActions('diary', ['setData', 'getContent']),
    open () {
      this.$prompt('请输入新分类名称', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      })
        .then(({ value }) => {
          const item = {
            title: value,
            type: 1
          }
          DiaryApi.save(item)
            .then(res => {
              if (res.code === 200) {
                this.initItem()
              } else {
                this.$message.error(res.msg)
              }
            })
            .catch(e => {
              console.error(e)
            })
          this.$message({
            type: 'success',
            message: '创建分类成功'
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '操作取消'
          })
        })
    },
    /**
     * 抽屉提交
     */
    submit () {
      this.dialogFormVisible = true
    },
    handleClose (done) {
      this.$confirm('确定要保存吗？')
        .then(_ => {
          this.loading = true
          setTimeout(() => {
            this.loading = false
            done()
            this.saveContent()
          }, 1000)
        })
        .catch(_ => {})
    },
    /**
     * websocket
     */
    getConfigResult (data) {
      console.log('websocel', data)
      if (data.status === 404 && data.msg === 'logout') {
        this.$notify({
          title: '退出登录提醒',
          message: '您的账号已在别处登录,请您修改密码或重新登录',
          type: 'warning',
          duration: 5000,
          showClose: false,
          offset: '50%'
        })
        LogOut()
          .then(res => {
            if (res.code === 200) {
              this.removeUserInfo('logout')
            } else {
              this.$message.error(res.msg)
            }
          })
          .catch(e => {
            console.error(e)
          })
      }
      /**
       * token 过期
       */
      if (data.status === 203) {
        this.$message.error('token 过期，请重新登录')
        this.removeUserInfo()
      }
    },
    /**
     * 移除用户信息
     */
    removeUserInfo () {
      localStorage.removeItem('userInfo')
      localStorage.removeItem('loginInfo')
      localStorage.removeItem('token')
      this.$router.push({ path: '/' })
    },
    /**
     * 用户下限
     */
    handleCommand (command) {
      if (command === 'logout') {
        LogOut()
          .then(res => {
            if (res.code === 200) {
              this.$message('退出成功')
              this.removeUserInfo('logout')
            }
          })
          .catch(e => {
            console.error(e)
          })
      }
    },
    /**
     * 初始化分类
     */
    initItem () {
      DiaryApi.getItem().then(res => {
        if (res.code === 200) {
          this.itemData = res.data
          this.getDiary(this.itemData[0])
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    /**
     * 展示编辑器
     */
    openEdit (type) {
      this.edit.content = ''
      this.form = {}
      this.noteType = type
      this.showEdit = true
    },
    /**
     * 获取日记
     */
    getDiary (item) {
      if (item === undefined) {
        return
      }
      this.pageNum = 1
      // 设置显示list
      this.link = 'show'
      this.itemObject = item
      EncryptionApi.check({ targetId: item.id }).then(check => {
        // false 没有加密 true 加密
        if (check.code === 200 && check.data === false) {
          this.getNoteByItem(item.id)
        } else {
          this.$prompt('请输入密码', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputType: 'password'
          })
            .then(({ value }) => {
              const params = {
                targetId: item.id,
                password: Check.cryptPwd(value)
              }
              EncryptionApi.verify(params).then(res => {
                if (res.code === 200 && res.data === true) {
                  this.getNoteByItem(item.id)
                } else {
                  this.$message({
                    type: 'error',
                    message: res.msg
                  })
                }
              })
            })
            .catch(() => {
              this.$message({
                type: 'info',
                message: '取消输入'
              })
            })
        }
      })
    },
    getNoteByItem (params) {
      const select = {
        itemId: params,
        pageNum: this.pageNum,
        pageSize: 10
      }
      DiaryApi.getNoteByItem(select).then(res => {
        this.showEdit = false
        if (res.code === 200) {
          console.log('查询返回结果:', res)
          this.diaryData = res.data
          this.diaryTotal = res.total
        } else {
          this.diaryData = []
          this.$message.error(res.msg)
        }
      })
    },
    /**
     * 保存文件内容
     */
    saveContent () {
      const suffix = this.noteType
      // if (suffix === 'html') {
      //   this.edit.content = this.$refs.ue.getUEContent()
      // } else {
      //   this.edit.content = this.$refs.markdown.getMDContent()
      // }
      const item = {
        title: this.form.name,
        type: 0,
        content: this.edit.content,
        itemId: this.form.item,
        suffix: suffix,
        id: this.form.id
      }
      if (this.showContent.id === item.id) {
        item.tagList = this.showContent.tagList
      }
      this.showContent.title = this.form.name
      DiaryApi.save(item).then(res => {
        if (res.code === 200) {
          // 添加此id防止保存一次成功之后再次添加没有id进行重复添加
          this.form.id = res.data.id
          this.$message({
            message: res.msg,
            type: 'success'
          })
          this.dialogFormVisible = false
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    /**
     * 展示编辑器内容
     */
    showEditContent (data) {
      this.showEdit = true
      this.noteType = data.suffix
      this.edit.content = data.content
      this.form.name = data.title
      this.form.item = data.itemId
      this.form.id = data.id
    },
    /**
     * 删除
     */
    handleDelete (row) {
      DiaryApi.deleteDiary(row.id).then(res => {
        if (res.code === 200) {
          console.log('删除数据返回', res)
          this.getDiary({ id: res.data[0].itemId })
          this.$message({
            message: res.msg,
            type: 'success'
          })
        }
      })
    },
    /**
     * 页码选中回调
     */
    selectPageNum (pageNum) {
      this.pageNum = pageNum
      this.getNoteByItem(this.itemObject.id)
    },
    /**
     * 获取编辑器的纯文本
     */
    getEditorContent (data) {
      console.log('通用的获取内容', data)
      this.edit.content = data
      this.dialogFormVisible = true
    }
  },
  mounted () {
    const token = Check.checkToken()
    if (!token) {
      this.$message.error('token过期，请重新登录')
    }
    const json = {
      msg: '测试websocket',
      status: 200
    }
    this.socketApi.sendSock(json, this.getConfigResult)
    this.initItem()
  }
}
</script>
<style>
html,
body,
#app {
  height: 100%;
}
.el-header,
.el-footer {
  background-color: #b3c0d1;
  color: #333;
  text-align: center;
  line-height: 60px;
}
.el-menu,
.el-menu--horizontal > .el-menu-item:not(.is-disabled):focus,
.el-menu--horizontal > .el-menu-item:not(.is-disabled):hover,
.el-menu--horizontal > .el-submenu .el-submenu__title:hover {
  background-color: #d3dce6 !important;
}
.el-menu {
  background-color: #d3dce6;
}
.el-aside {
  background-color: #d3dce6;
  color: #333;
  text-align: center;
  line-height: 200px;
}

.el-main {
  /* background-color: #e9eef3; */
  color: #333;
  text-align: center;
  /* padding: 0px; */
  /* line-height: 160px; */
}

body > .el-container {
  margin-bottom: 40px;
}

.el-container:nth-child(5) .el-aside,
.el-container:nth-child(6) .el-aside {
  line-height: 260px;
}

.el-container:nth-child(7) .el-aside {
  line-height: 320px;
}
.tree-col {
  background-color: #d3dce6;
  color: #333;
}
li {
  list-style: none;
  line-height: 30px;
}
.hei {
  height: 100% !important;
}
#edui1,
#editor,
#edui1_iframeholder {
  height: 100% !important;
  width: 100% !important;
}
.el-form-item__label {
  color: #606266 !important;
}
.el-sup {
  position: absolute;
  top: -2%;
  left: 5%;
}
.el-title {
  position: absolute;
  left: 14%;
}
.el-setter {
  position: absolute;
  top: 1%;
  right: 1%;
}
</style>
