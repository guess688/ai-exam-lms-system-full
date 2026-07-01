<template>
  <div class="app-container">
    <h3>组卷信息</h3>
    <el-tabs type="border-card" @tab-click="handleClick" v-model="activeName">
      <el-tab-pane label="自己选题" name="first">
        <ChooseQuestion
          @selected-change="handleSelectedChange"
          ref="questionSelector"
        ></ChooseQuestion>
      </el-tab-pane>
      <el-tab-pane label="随机抽题" name="second">
        <!-- <el-card style="margin-top: 20px"> -->
        <div style="float: right; font-weight: bold; color: #ff0000">
          试卷总分：{{ postForm.totalScore }}分
        </div>

        <div>
          <!-- <el-button class="filter-item" size="small" type="primary" icon="el-icon-plus" @click="handleAdd">
          添加题库
        </el-button> -->

          <el-table
            :data="repoList"
            :border="false"
            empty-text="请点击上面的`添加题库`进行设置"
            style="width: 100%; margin-top: 15px"
          >
            <el-table-column label="题库" width="200">
              <template v-slot="scope">
                <repo-select
                  v-model="scope.row.repoId"
                  :multi="false"
                  :excludes="excludes"
                  @change="repoChange($event, scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="单选数量" align="center">
              <template v-slot="scope">
                <el-input-number
                  v-model="scope.row.radioCount"
                  :min="0"
                  :max="scope.row.totalRadio"
                  :controls="false"
                  style="width: 80px"
                />
                <template v-if="scope.row.totalRadio != undefined">
                  / {{ scope.row.totalRadio }}
                </template>
              </template>
            </el-table-column>

            <el-table-column label="单选分数" align="center">
              <template v-slot="scope">
                <el-input-number
                  v-model="scope.row.radioScore"
                  :min="0"
                  :controls="false"
                  style="width: 80px"
                />
              </template>
            </el-table-column>

            <el-table-column label="多选数量" align="center">
              <template v-slot="scope">
                <el-input-number
                  v-model="scope.row.multiCount"
                  :min="0"
                  :max="scope.row.totalMulti"
                  :controls="false"
                  style="width: 80px"
                />
                <template v-if="scope.row.totalMulti != undefined">
                  / {{ scope.row.totalMulti }}
                </template>
              </template>
            </el-table-column>

            <el-table-column label="多选分数" align="center">
              <template v-slot="scope">
                <el-input-number
                  v-model="scope.row.multiScore"
                  :min="0"
                  :controls="false"
                  style="width: 80px"
                />
              </template>
            </el-table-column>

            <el-table-column label="判断题数量" align="center">
              <template v-slot="scope">
                <el-input-number
                  v-model="scope.row.judgeCount"
                  :min="0"
                  :max="scope.row.totalJudge"
                  :controls="false"
                  style="width: 80px"
                />
                <template v-if="scope.row.totalJudge != undefined">
                  / {{ scope.row.totalJudge }}
                </template>
              </template>
            </el-table-column>

            <el-table-column label="判断题分数" align="center">
              <template v-slot="scope">
                <el-input-number
                  v-model="scope.row.judgeScore"
                  :min="0"
                  :controls="false"
                  style="width: 80px"
                />
              </template>
            </el-table-column>
            <el-table-column label="简答题数量" align="center">
              <template v-slot="scope">
                <el-input-number
                  v-model="scope.row.saqCount"
                  :min="0"
                  :max="scope.row.totalSaq"
                  :controls="false"
                  style="width: 80px"
                />
                <template v-if="scope.row.totalSaq != undefined">
                  / {{ scope.row.totalSaq }}
                </template>
              </template>
            </el-table-column>

            <el-table-column label="简答题分数" align="center">
              <template v-slot="scope">
                <el-input-number
                  v-model="scope.row.saqScore"
                  :min="0"
                  :controls="false"
                  style="width: 80px"
                />
              </template>
            </el-table-column>
            <!-- <el-table-column label="删除" align="center" width="80px">
            <template v-slot="scope">
              <el-button
                type="danger"
                icon="el-icon-delete"
                circle
                @click="removeItem(scope.$index)"
              />
            </template>
          </el-table-column> -->
          </el-table>
        </div>
        <!-- </el-card> -->
      </el-tab-pane>
      <el-tab-pane label="智能自动组卷" name="third">
        <div class="auto-paper-panel">
          <div class="auto-score">试卷总分：{{ autoTotalScore }} 分</div>
          <el-form :model="autoForm" label-width="130px" class="auto-paper-form">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="课程">
                  <el-select
                    v-model="autoForm.courseId"
                    clearable
                    filterable
                    placeholder="请选择课程"
                    style="width: 100%"
                    @change="handleAutoCourseChange"
                  >
                    <el-option v-for="item in courseOptions" :key="item.id" :label="item.name" :value="item.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="章节范围">
                  <el-select
                    v-model="autoForm.chapterIds"
                    multiple
                    collapse-tags
                    clearable
                    filterable
                    :disabled="!autoForm.courseId"
                    placeholder="可多选章节"
                    style="width: 100%"
                    @change="handleAutoChapterChange"
                  >
                    <el-option v-for="item in autoChapterOptions" :key="item.id" :label="item.title" :value="item.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="知识点范围">
                  <el-select
                    v-model="autoForm.knowledgePointIds"
                    multiple
                    collapse-tags
                    clearable
                    filterable
                    :disabled="!autoForm.courseId"
                    placeholder="可多选知识点"
                    style="width: 100%"
                  >
                    <el-option v-for="item in autoKnowledgePointOptions" :key="item.id" :label="item.name" :value="item.id" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-table :data="autoTypeRows" border style="width: 100%; margin-top: 10px">
              <el-table-column prop="label" label="题型" min-width="160" />
              <el-table-column label="数量" align="center" width="180">
                <template v-slot="scope">
                  <el-input-number v-model="autoForm[scope.row.countKey]" :min="0" :controls="false" style="width: 110px" />
                </template>
              </el-table-column>
              <el-table-column label="每题分值" align="center" width="180">
                <template v-slot="scope">
                  <el-input-number v-model="autoForm[scope.row.scoreKey]" :min="0" :controls="false" style="width: 110px" />
                </template>
              </el-table-column>
            </el-table>

            <el-form-item label="难度配置" style="margin-top: 18px">
              <el-radio-group v-model="autoForm.difficultyMode">
                <el-radio-button label="ratio">按比例</el-radio-button>
                <el-radio-button label="count">按数量</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-row v-if="autoForm.difficultyMode === 'ratio'" :gutter="20">
              <el-col :span="8">
                <el-form-item label="简单比例">
                  <el-input-number v-model="autoForm.easyRatio" :min="0" :controls="false" style="width: 140px" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="中等比例">
                  <el-input-number v-model="autoForm.mediumRatio" :min="0" :controls="false" style="width: 140px" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="困难比例">
                  <el-input-number v-model="autoForm.hardRatio" :min="0" :controls="false" style="width: 140px" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row v-else :gutter="20">
              <el-col :span="8">
                <el-form-item label="简单数量">
                  <el-input-number v-model="autoForm.easyCount" :min="0" :controls="false" style="width: 140px" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="中等数量">
                  <el-input-number v-model="autoForm.mediumCount" :min="0" :controls="false" style="width: 140px" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="困难数量">
                  <el-input-number v-model="autoForm.hardCount" :min="0" :controls="false" style="width: 140px" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>

    <h3>考试配置</h3>
    <el-card style="margin-top: 20px">
      <el-form
        ref="postForm"
        :model="postForm"
        :rules="rules"
        label-position="left"
        label-width="120px"
      >
        <el-form-item label="考试名称" prop="title">
          <el-input v-model="postForm.title" />
        </el-form-item>

        <!-- <el-form-item label="考试描述" prop="content">
          <el-input v-model="postForm.content" type="textarea" />
        </el-form-item> -->

        <el-form-item label="总分数" prop="totalScore">
          <el-input-number :value="activeName === 'third' ? autoTotalScore : postForm.totalScore" disabled />
        </el-form-item>

        <el-form-item label="及格分" prop="passedScore">
          <el-input-number
            v-model="postForm.passedScore"
            :max="activeName === 'third' ? autoTotalScore : postForm.totalScore"
          />
        </el-form-item>

        <el-form-item label="最多切屏次数" prop="maxCount">
          <el-input-number v-model="postForm.maxCount" />
        </el-form-item>
        <el-form-item label="证书" prop="maxCount">
          <CertificateSelect
            v-model="postForm.certificateId"
            is-multiple
            @change="onCertificateChange"
          />
          <!-- <el-input-number v-model="postForm.maxCount"  /> -->
        </el-form-item>
        <el-form-item label="考试时长(分钟)" prop="examDuration">
          <el-input-number v-model="postForm.examDuration" />
        </el-form-item>
        <el-form-item label="考试时间范围" prop="start">
          <el-date-picker
            v-model="postForm.start"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <!--
        <el-form-item label="是否限时">
          <el-checkbox v-model="postForm.timeLimit" />
        </el-form-item> -->

        <!-- <el-form-item
          v-if="postForm.timeLimit"
          label="考试时间"
          prop="examDuration"
        >
          <el-date-picker
            v-model="dateValues"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item> -->
      </el-form>
    </el-card>

    <h3>权限配置</h3>
    <el-card style="margin-top: 20px">
      <div style="display: flex">
        <div style="margin-left: 10px">
          <el-form
            ref="postForm"
            :model="postForm"
            :rules="rules"
            label-position="left"
            label-width="120px"
          >
            <el-form-item label="考试班级" prop="classIds">
              <ClassSelect
                v-model="postForm.classIds"
                is-multiple
                @change="onClassChange"
              />
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>

    <div style="margin-top: 20px">
      <el-button type="primary" @click="handleSave">保存</el-button>
    </div>
  </div>
</template>

<script>
// import { fetchDetail } from '@/api/exam'
import RepoSelect from "@/components/RepoSelect";
import ClassSelect from "@/components/ClassSelect";
import CertificateSelect from "@/components/CertificateSelect";
import { autoGenerateExam, saveData } from "@/api/exam";
import { fetchCourseChapters, fetchCoursePage, fetchKnowledgePoints } from "@/api/course";
import ChooseQuestion from "@/components/ExamComponents/ChooseQuestion";
export default {
  name: "ExamDetail",

  components: {
    RepoSelect,
    ChooseQuestion,
    ClassSelect,
    CertificateSelect,
  },
  data() {
    return {
      activeName: "first",
      input: "",
      treeData: [],
      defaultProps: {
        label: "deptName",
      },
      filterText: "",
      treeLoading: false,
      courseOptions: [],
      autoChapterOptions: [],
      autoKnowledgePointOptions: [],
      autoTypeRows: [
        { label: "单选题", countKey: "radioCount", scoreKey: "radioScore" },
        { label: "多选题", countKey: "multiCount", scoreKey: "multiScore" },
        { label: "不定项选择题", countKey: "indefiniteCount", scoreKey: "indefiniteScore" },
        { label: "判断题", countKey: "judgeCount", scoreKey: "judgeScore" },
      ],
      autoForm: {
        courseId: "",
        chapterIds: [],
        knowledgePointIds: [],
        radioCount: 0,
        radioScore: 0,
        multiCount: 0,
        multiScore: 0,
        indefiniteCount: 0,
        indefiniteScore: 0,
        judgeCount: 0,
        judgeScore: 0,
        difficultyMode: "ratio",
        easyCount: 0,
        mediumCount: 0,
        hardCount: 0,
        easyRatio: 4,
        mediumRatio: 4,
        hardRatio: 2,
      },
      // dateValues: [],
      // 题库
      repoList: [
        {
          addQuType: "0",
          queIds: "",
          id: "",
          rowId: 0,
          radioCount: 0,
          radioScore: 0,
          multiCount: 0,
          multiScore: 0,
          judgeCount: 0,
          judgeScore: 0,
          saqCount: 0,
          saqScore: 0,
        },
      ],
      // 已选择的题库
      excludes: [],
      postForm: {
        start: [],
        // 总分数
        totalScore: 0,
        // 题库列表
        repoList: [],
        // 开放类型
        openType: 1,
        // 考试班级列表
        departIds: [],
        // 初始化班级列表
        classIds: [],
      },
      rules: {
        title: [{ required: true, message: "考试名称不能为空！" }],

        // content: [{ required: true, message: '考试描述不能为空！' }],

        open: [{ required: true, message: "考试权限不能为空！" }],

        totalScore: [{ required: true, message: "考试分数不能为空！" }],

        passedScore: [{ required: true, message: "及格分不能为空！" }],

        examDuration: [{ required: true, message: "考试时间不能为空！" }],

        start: [{ required: true, message: "考试时间范围不能为空！" }],

        maxCount: [{ required: false, message: "最多切屏次数" }],
        classIds: [
          {
            required: true,
            message: "请选择考试班级！",
            type: "array",
            min: 1,
          },
        ],
      },
    };
  },

  computed: {
    autoQuestionTotal() {
      return (
        Number(this.autoForm.radioCount || 0) +
        Number(this.autoForm.multiCount || 0) +
        Number(this.autoForm.indefiniteCount || 0) +
        Number(this.autoForm.judgeCount || 0)
      );
    },
    autoTotalScore() {
      return (
        Number(this.autoForm.radioCount || 0) * Number(this.autoForm.radioScore || 0) +
        Number(this.autoForm.multiCount || 0) * Number(this.autoForm.multiScore || 0) +
        Number(this.autoForm.indefiniteCount || 0) * Number(this.autoForm.indefiniteScore || 0) +
        Number(this.autoForm.judgeCount || 0) * Number(this.autoForm.judgeScore || 0)
      );
    },
  },

  created() {
    this.loadCourses();
  },

  watch: {
    filterText(val) {
      this.$refs.tree.filter(val);
    },

    // dateValues: {
    //   handler() {
    //     this.postForm.startTime = this.dateValues[0];
    //     this.postForm.endTime = this.dateValues[1];
    //   },
    // },

    // 题库变换
    repoList: {
      handler(val) {
        let totalScore = 0;
        this.excludes = [];
        for (let i = 0; i < val.length; i++) {
          const item = val[i];
          if (item.radioCount > 0 && item.radioScore > 0) {
            totalScore += item.radioCount * item.radioScore;
          }

          if (item.multiCount > 0 && item.multiScore > 0) {
            totalScore += item.multiCount * item.multiScore;
          }

          if (item.judgeCount > 0 && item.judgeScore > 0) {
            totalScore += item.judgeCount * item.judgeScore;
          }
          if (item.saqCount > 0 && item.saqScore > 0) {
            totalScore += item.saqCount * item.saqScore;
          }
          this.excludes.push(item.id);
        }

        // 赋值
        this.postForm.totalScore = totalScore;
        this.postForm.repoList = val;
        this.$forceUpdate();
      },
      deep: true,
    },
  },
  methods: {
    async loadCourses() {
      const res = await fetchCoursePage({ pageNum: 1, pageSize: 1000, status: 1 });
      this.courseOptions = (res.data && res.data.records) || [];
    },
    async handleAutoCourseChange(courseId) {
      this.autoForm.chapterIds = [];
      this.autoForm.knowledgePointIds = [];
      this.autoChapterOptions = [];
      this.autoKnowledgePointOptions = [];
      if (!courseId) {
        return;
      }
      const res = await fetchCourseChapters(courseId, { status: 1 });
      this.autoChapterOptions = res.data || [];
    },
    async handleAutoChapterChange(chapterIds) {
      this.autoForm.knowledgePointIds = [];
      this.autoKnowledgePointOptions = [];
      if (!this.autoForm.courseId) {
        return;
      }
      if (!chapterIds || chapterIds.length === 0) {
        const chapterRes = await fetchCourseChapters(this.autoForm.courseId, { status: 1 });
        const chapters = chapterRes.data || [];
        for (const chapter of chapters) {
          const res = await fetchKnowledgePoints(chapter.id, { status: 1 });
          this.autoKnowledgePointOptions.push(...(res.data || []));
        }
        return;
      }
      for (const chapterId of chapterIds) {
        const res = await fetchKnowledgePoints(chapterId, { status: 1 });
        this.autoKnowledgePointOptions.push(...(res.data || []));
      }
    },
    // 点击tab
    handleClick(tab, event) {
      if (this.$refs.questionSelector) {
        this.$refs.questionSelector.clearSelection();
      }
      // console.log(event)
      this.repoList[0].addQuType = tab.index;
      this.repoList[0].queIds = "";
      this.repoList[0].id = "";
      this.repoList[0].rowId = 0;
      this.repoList[0].radioCount = 0;
      this.repoList[0].radioScore = 0;
      this.repoList[0].multiCount = 0;
      this.repoList[0].judgeCount = 0;
      this.repoList[0].judgeScore = 0;
      this.repoList[0].saqCount = 0;
      this.repoList[0].saqScore = 0;
      console.log(tab, event);
    },
    // 子组件选择的ids
    handleSelectedChange(selectedIds) {
      var ids = [];
      selectedIds.selectedRows.forEach((item) => {
        ids.push(item.id);
      });

      this.repoList[0].queIds = ids.join(",");
      this.repoList[0].radioCount = selectedIds.questionList.radioCount;
      this.repoList[0].radioScore = selectedIds.questionList.radioScore;
      this.repoList[0].multiCount = selectedIds.questionList.multiCount;
      this.repoList[0].multiScore = selectedIds.questionList.multiScore;
      this.repoList[0].judgeCount = selectedIds.questionList.judgeCount;
      this.repoList[0].judgeScore = selectedIds.questionList.judgeScore;
      this.repoList[0].saqCount = selectedIds.questionList.saqCount;
      this.repoList[0].saqScore = selectedIds.questionList.saqScore;
      console.log("从子组件接收到的选中ID:", this.repoList);
      // 在这里你可以将选中的ID保存到父组件的数据中
      this.selectedQuestionIds = selectedIds;
      // 或者执行其他需要的操作
    },
    handleAutoSave() {
      this.$refs.postForm.validate((valid) => {
        if (!valid) {
          return;
        }
        if (!this.autoForm.courseId) {
          this.$message.warning("请选择课程");
          return;
        }
        if (!this.postForm.classIds || this.postForm.classIds.length === 0) {
          this.$message.warning("请选择考试班级");
          return;
        }
        if (this.autoQuestionTotal <= 0 || this.autoTotalScore <= 0) {
          this.$message.warning("请至少配置一种题型的数量和分值");
          return;
        }
        const invalidType = this.autoTypeRows.some((row) => {
          const count = Number(this.autoForm[row.countKey] || 0);
          const score = Number(this.autoForm[row.scoreKey] || 0);
          return (count > 0 && score <= 0) || (count === 0 && score > 0);
        });
        if (invalidType) {
          this.$message.warning("题型数量和分值需要同时配置");
          return;
        }
        if (this.autoForm.difficultyMode === "count") {
          const difficultyTotal =
            Number(this.autoForm.easyCount || 0) +
            Number(this.autoForm.mediumCount || 0) +
            Number(this.autoForm.hardCount || 0);
          if (difficultyTotal !== this.autoQuestionTotal) {
            this.$message.warning("难度数量合计必须等于题目总数");
            return;
          }
        } else {
          const ratioTotal =
            Number(this.autoForm.easyRatio || 0) +
            Number(this.autoForm.mediumRatio || 0) +
            Number(this.autoForm.hardRatio || 0);
          if (ratioTotal <= 0) {
            this.$message.warning("难度比例合计必须大于0");
            return;
          }
        }

        this.$confirm("确定按当前条件自动生成试卷吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        }).then(() => {
          this.submitAutoForm();
        });
      });
    },
    handleSave() {
      if (this.activeName === "third") {
        this.handleAutoSave();
        return;
      }
      if (this.repoList[0].addQuType === "1") {
        this.$refs.postForm.validate((valid) => {
          if (!valid) {
            return;
          }
          if (this.postForm.totalScore === 0) {
            this.$notify({
              title: "提示信息",
              message: "考试规则设置不正确，请确认！",
              type: "warning",
              duration: 2000,
            });

            return;
          }

          // 验证班级是否选择
          if (!this.postForm.classIds || this.postForm.classIds.length === 0) {
            this.$notify({
              title: "提示信息",
              message: "请选择考试班级！",
              type: "warning",
              duration: 2000,
            });
            return;
          }

          for (let i = 0; i < this.postForm.repoList.length; i++) {
            const repo = this.postForm.repoList[i];
            if (!repo.repoId) {
              this.$notify({
                title: "提示信息",
                message: "考试题库选择不正确！",
                type: "warning",
                duration: 2000,
              });
              return;
            }

            if (
              (repo.radioCount > 0 && repo.radioScore === 0) ||
              (repo.radioCount === 0 && repo.radioScore > 0)
            ) {
              this.$notify({
                title: "提示信息",
                message: "题库第：[" + (i + 1) + "]项存在无效的单选题配置！",
                type: "warning",
                duration: 2000,
              });

              return;
            }

            if (
              (repo.multiCount > 0 && repo.multiScore === 0) ||
              (repo.multiCount === 0 && repo.multiScore > 0)
            ) {
              this.$notify({
                title: "提示信息",
                message: "题库第：[" + (i + 1) + "]项存在无效的多选题配置！",
                type: "warning",
                duration: 2000,
              });

              return;
            }

            if (
              (repo.judgeCount > 0 && repo.judgeScore === 0) ||
              (repo.judgeCount === 0 && repo.judgeScore > 0)
            ) {
              this.$notify({
                title: "提示信息",
                message: "题库第：[" + (i + 1) + "]项存在无效的判断题配置！",
                type: "warning",
                duration: 2000,
              });
              return;
            }

            if (
              (repo.saqCount > 0 && repo.saqScore === 0) ||
              (repo.saqCount === 0 && repo.saqScore > 0)
            ) {
              this.$notify({
                title: "提示信息",
                message: "题库第：[" + (i + 1) + "]项存在无效的简答题配置！",
                type: "warning",
                duration: 2000,
              });
              return;
            }
          }

          this.$confirm("确实要提交保存吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          }).then(() => {
            this.submitForm();
          });
        });
      }
      if (this.repoList[0].addQuType === "0") {
        this.$confirm("确实要提交保存吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        }).then(() => {
          this.submitForm();
        });
      }
    },

    handleCheckChange() {
      const that = this;
      // 置空
      this.postForm.departIds = [];
      const nodes = this.$refs.tree.getCheckedNodes();
      nodes.forEach(function (item) {
        that.postForm.departIds.push(item.id);
      });
    },

    // 添加子项
    handleAdd() {
      this.repoList.push();
    },

    removeItem(index) {
      this.repoList.splice(index, 1);
    },
    formatDateToISOString(date) {
      if (!(date instanceof Date)) {
        return null;
      }

      // 获取本地时间的各部分（不进行时区转换）
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const hours = String(date.getHours()).padStart(2, "0");
      const minutes = String(date.getMinutes()).padStart(2, "0");
      const seconds = String(date.getSeconds()).padStart(2, "0");

      // 格式化为本地时间字符串（不含时区信息）
      return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
      // 输出示例："2025-05-14 09:00:00"（保留用户选择的本地时间）
    },

    submitAutoForm() {
      const certificateId = Array.isArray(this.postForm.certificateId)
        ? this.postForm.certificateId[0]
        : this.postForm.certificateId;
      const params = {
        title: this.postForm.title,
        examDuration: this.postForm.examDuration,
        maxCount: this.postForm.maxCount,
        passedScore: this.postForm.passedScore,
        totalScore: this.autoTotalScore,
        startTime: this.formatDateToISOString(this.postForm.start[0]),
        endTime: this.formatDateToISOString(this.postForm.start[1]),
        gradeIds: this.postForm.classIds.join(","),
        certificateId: certificateId || null,
        courseId: this.autoForm.courseId,
        chapterIds: this.autoForm.chapterIds,
        knowledgePointIds: this.autoForm.knowledgePointIds,
        radioCount: this.autoForm.radioCount,
        radioScore: this.autoForm.radioScore,
        multiCount: this.autoForm.multiCount,
        multiScore: this.autoForm.multiScore,
        judgeCount: this.autoForm.judgeCount,
        judgeScore: this.autoForm.judgeScore,
        indefiniteCount: this.autoForm.indefiniteCount,
        indefiniteScore: this.autoForm.indefiniteScore,
        saqCount: this.autoForm.indefiniteCount,
        saqScore: this.autoForm.indefiniteScore,
      };
      if (this.autoForm.difficultyMode === "count") {
        params.easyCount = this.autoForm.easyCount;
        params.mediumCount = this.autoForm.mediumCount;
        params.hardCount = this.autoForm.hardCount;
      } else {
        params.easyRatio = this.autoForm.easyRatio;
        params.mediumRatio = this.autoForm.mediumRatio;
        params.hardRatio = this.autoForm.hardRatio;
      }
      autoGenerateExam(params).then((res) => {
        if (res.code) {
          this.$notify({
            title: "成功",
            message: "自动组卷成功",
            type: "success",
            duration: 2000,
          });
          this.$router.push({ name: "exam-management" });
        } else {
          this.$notify({
            title: "失败",
            message: res.msg,
            type: "error",
            duration: 3000,
          });
        }
      });
    },

    submitForm() {
      console.log("postForm", this.postForm);
      // 校验和处理数据
      let cerTemp = "";
      if (
        this.postForm.certificateId != null &&
        this.postForm.certificateId != ""
      ) {
        console.log(this.postForm.certificateId);
        cerTemp = this.postForm.certificateId.join(",");
      }
      this.postForm.repoList = this.repoList;

      const params = {
        title: this.postForm.title,
        // content: this.postForm.content, // 添加考试描述字段
        examDuration: this.postForm.examDuration,
        maxCount: this.postForm.maxCount,
        passedScore: this.postForm.passedScore,
        startTime: this.formatDateToISOString(this.postForm.start[0]),
        endTime: this.formatDateToISOString(this.postForm.start[1]),
        gradeIds: this.postForm.classIds.join(","),
        repoId: this.postForm.repoList[0].repoId,
        certificateId: cerTemp,
        addQuype: this.postForm.repoList[0].addQuType,
        quIds: this.postForm.repoList[0].queIds,
        radioCount: this.postForm.repoList[0].radioCount,
        radioScore: this.postForm.repoList[0].radioScore,
        multiCount: this.postForm.repoList[0].multiCount,
        multiScore: this.postForm.repoList[0].multiScore,
        judgeCount: this.postForm.repoList[0].judgeCount,
        judgeScore: this.postForm.repoList[0].judgeScore,
        saqCount: this.postForm.repoList[0].saqCount,
        saqScore: this.postForm.repoList[0].saqScore,
      };
      saveData(params).then((res) => {
        if (res.code) {
          this.$notify({
            title: "成功",
            message: "考试保存成功！",
            type: "success",
            duration: 2000,
          });

          this.$router.push({ name: "exam-management" });
        } else {
          this.$notify({
            title: "失败",
            message: res.msg,
            type: "error",
            duration: 2000,
          });
        }
      });
    },

    filterNode(value, data) {
      if (!value) return true;
      return data.deptName.indexOf(value) != -1;
    },
    onCertificateChange() {
      // 方法实现...
    },
    onClassChange() {},
    repoChange(e, row) {
      // 赋值ID
      row.id = e.id;
      if (e != null) {
        row.totalRadio = e.radioNum;
        row.totalMulti = e.multiNum;
        row.totalJudge = e.judgeNum;
        row.totalSaq = e.saqNum;
      } else {
        row.totalRadio = 0;
        row.totalMulti = 0;
        row.totalJudge = 0;
        row.totalSaq = 0;
      }
    },
  },
};
</script>

<style scoped>
.auto-paper-panel {
  padding: 8px 0 0;
}

.auto-score {
  text-align: right;
  font-weight: 600;
  color: #f56c6c;
  margin-bottom: 12px;
}

.auto-paper-form {
  clear: both;
}
</style>
