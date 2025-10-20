<template>
  <div class="item-form">
    <div class="form-container">
      <h1>{{ isEdit ? '编辑物品' : '添加物品' }}</h1>
      
      <form @submit.prevent="handleSubmit">
        <div class="form-row">
          <div class="form-group">
            <label for="name">物品名称 *</label>
            <input
              id="name"
              v-model="form.name"
              type="text"
              required
              placeholder="请输入物品名称"
            />
          </div>
          
          <div class="form-group">
            <label for="brand">品牌</label>
            <input
              id="brand"
              v-model="form.brand"
              type="text"
              placeholder="请输入品牌"
            />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="purchasePrice">购买价格 *</label>
            <input
              id="purchasePrice"
              v-model="form.purchasePrice"
              type="number"
              step="0.01"
              min="0"
              required
              placeholder="0.00"
            />
          </div>
          
          <div class="form-group">
            <label for="purchaseDate">购买日期 *</label>
            <input
              id="purchaseDate"
              v-model="form.purchaseDate"
              type="date"
              required
            />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="size">尺码</label>
            <input
              id="size"
              v-model="form.size"
              type="text"
              placeholder="如：M, L, 42, 43"
            />
          </div>
          
          <div class="form-group">
            <label for="color">颜色</label>
            <input
              id="color"
              v-model="form.color"
              type="text"
              placeholder="请输入颜色"
            />
          </div>
        </div>

        <div class="form-group">
          <label for="location">存放位置</label>
          <input
            id="location"
            v-model="form.location"
            type="text"
            placeholder="如：衣柜、鞋盒、房间"
          />
        </div>

        <div class="form-group">
          <label>图片上传</label>
          <div class="image-upload">
            <input
              ref="fileInput"
              type="file"
              multiple
              accept="image/*"
              @change="handleFileUpload"
              style="display: none"
            />
            <button type="button" @click="$refs.fileInput.click()" class="upload-btn">
              选择图片
            </button>
            <div v-if="form.images.length > 0" class="image-preview">
              <div v-for="(image, index) in form.images" :key="index" class="image-item">
                <img :src="image" :alt="`图片${index + 1}`" />
                <button type="button" @click="removeImage(index)" class="remove-btn">×</button>
              </div>
            </div>
          </div>
        </div>

        <div class="form-actions">
          <router-link to="/items" class="btn-secondary">取消</router-link>
          <button type="submit" :disabled="loading" class="btn-primary">
            {{ loading ? '保存中...' : '保存' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const isEdit = ref(false)
const loading = ref(false)

const form = ref({
  name: '',
  brand: '',
  purchasePrice: '',
  purchaseDate: '',
  size: '',
  color: '',
  location: '',
  images: [] as string[]
})

const handleFileUpload = (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  
  if (files) {
    Array.from(files).forEach(file => {
      if (file.type.startsWith('image/')) {
        const reader = new FileReader()
        reader.onload = (e) => {
          const result = e.target?.result as string
          if (result) {
            form.value.images.push(result)
          }
        }
        reader.readAsDataURL(file)
      }
    })
  }
}

const removeImage = (index: number) => {
  form.value.images.splice(index, 1)
}

const handleSubmit = async () => {
  loading.value = true
  
  try {
    // TODO: 调用API保存物品
    console.log('保存物品:', form.value)
    
    // 模拟保存成功
    setTimeout(() => {
      router.push('/items')
    }, 1000)
  } catch (error) {
    console.error('保存失败:', error)
    alert('保存失败，请重试')
  } finally {
    loading.value = false
  }
}

const loadItem = () => {
  if (route.params.id) {
    isEdit.value = true
    // TODO: 根据ID加载物品数据
    console.log('加载物品:', route.params.id)
  }
}

onMounted(() => {
  loadItem()
})
</script>

<style scoped>
.item-form {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.form-container {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.form-container h1 {
  margin: 0 0 30px 0;
  color: #333;
  text-align: center;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #555;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.image-upload {
  border: 2px dashed #ddd;
  border-radius: 4px;
  padding: 20px;
  text-align: center;
}

.upload-btn {
  background: #667eea;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.image-preview {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 10px;
  margin-top: 15px;
}

.image-item {
  position: relative;
  aspect-ratio: 1;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.remove-btn {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 20px;
  height: 20px;
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 12px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
}

.btn-primary {
  background: #667eea;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  background: white;
  color: #333;
  padding: 10px 20px;
  border: 1px solid #ddd;
  border-radius: 4px;
  text-decoration: none;
  cursor: pointer;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
}
</style>
