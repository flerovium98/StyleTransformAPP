# StyleTransformAPP

首先引导界面有两个选项，上面的是人像模式，下面的是风景模式
 

1 人像模式
首先选择人像模式，要编辑的图片有两种获取方式，可以拍照或者从图库当中选择。
 
选择其中一种方式加载要编辑的图片，对图片的操作有两大类。
 


第一类是提供九种不同人脸部位的动画贴纸。
 

         

点击叠加贴纸可以增加一个图层，在上面随意放置第二个贴纸
 
 

可以叠加多个图层，效果如下
 

同时，如果觉得当前图层添加的不满意，可以点击撤销按钮。撤销当前图层，可以连续撤销
 






接下来是单图层的风格迁移，点击对应的风格按钮可以生成不同的融合风格图片，效果如下，由于计算量较大，所以点击后会缓存效果图片，第二次点击相同图片时就可以直接调用缓存的数据了
  





2风景画模式

也有两种对图像的操作，一种是简单的滤镜，另一种是复杂版的风格转换。
  

六种滤镜有不同的效果
下面是两张示例
  

接下来是复杂版的风格转换
可以选择单一风格转换，
下面是分别融合了一次不同名画的风格图片
  

因为风格迁移渲染的计算量比较大，所以对计算过的结果我们进行缓存进栈，下次点击相同风格转换时调用缓存的计算结果节省时间。

接下来可以叠加油画风格，下图是已经叠加了两层的效果，点击叠加按钮可以继续添加图层，在生成的图片上应用风格迁移，可以无限叠加，如下图。

  



多图层叠加的实现：

贴纸的多图层叠加和多风格叠加的实现的很多细节不同，但是总体步骤是相似的，我们将详细介绍总体步骤的实现。
首先创建一个类型的结构体，该类型的对象存储一个bitmap对象、用户点击对应bitmap对象的时间、该bitmap所在的图层信息。
再创建一个存储该类型的stack对象，出栈压栈操作对应添加图层和撤销操作。
栈底元素永远是在加载编辑图片时初始化，当调用摄像头或者照片图库获取图片时，对于人像模式，会调用已经训练好的人脸识别的函数，判断人脸的位置，判断各个五官的位置，并记录对应的坐标，如果检测到人脸，就将该bitmap打包信息，将其压入栈底，其包含的信息中bitmap永远是原图，所在图层信息初始化为0，并存入导入bitmap的时间。
在一个图层上的操作（切换贴纸、切换油画风格）会将当前的bitmap和点击时间等打包写入多个创建的全局容器来装载缓存信息，以防用户来回点击相同效果而重复计算，耗费时间和算力。
当点击添加图层时，遍历所有容器，返回有最大时间属性的容器对象，将该对象的图层信息加一，将该对象复制后压入创建的stack对象中，作为添加图层前的图层信息。
当点击撤销时，将栈顶元素弹出，显示当前栈顶元素其中的bitmap属性，即是撤销的效果。更新图层信息减一。
其中有一些容错机制，比如防止用户添加完图层后还没有进行任何操作后再一次点击叠加图层，通过设置标志位来解决，防止浪费内存空间。比如防止用户连续点击撤销导致对空栈进行弹出操作，当然，这里甚至不能出现空栈，因为栈底元素中的属性中一直保存着原图信息，要防止空栈丢失信息。等等一些防异常操作。



实验结果分析：


由于风格转换的过程和贴图过程不太一样，所以存储每一图层的bitmap信息的方式不太一样，风格转换较为复杂，计算量大，在单一图层就需要缓存每次处理的bitmap信息，由于风格转换函数需要，缓存的容器类型就已经打包了bitmap信息、和时间信息，我只需利用该结构体就可以，只用在其中加入校准用的图层信息即可；而人像贴图不需要很大计算量，点击特定的贴图会简单通过switch语句找到在人脸识别时存储的各个五官的像素坐标，将原图bitmap覆盖即可。于是刚开始我把风景画的stack类型定为我创建的结构体类型，人像模式的stack的存储类型定为bitmap，后来在出栈入栈时发现，存储的bitmap无法索引到正确的图层信息，或者说不方便索引，而且当用户在一个图层来回切换贴纸后点击叠加图层也不知道最后一个显示的bitmap的数据是哪个，于是我将人像模式的stack存储类型也变成特定的结构体，存放对应于bitmap的时间和图层信息用于索引查找和校准。
纵向的多图层叠加贴图和撤销弄完后需要考虑横向的按键之间的影响，比如有可能用户添加完图层后还没有进行任何操作后就再一次点击叠加图层，这样原本的程序中会向栈中压入一个信息几乎相同的结构体，造成资源浪费和错误（图层没有叠加但是图层信息加一）通过设置标志位来解决，防止浪费内存空间。比如防止用户连续点击撤销导致对空栈进行弹出操作，当然，这里甚至不能出现空栈，因为栈底元素中的属性中一直保存着原图信息，要防止空栈丢失信息。等等一些防异常操作。点击叠加图层后除了点击添加贴纸和改变风格外也都不能改变状态，即这种情况下不能再次点击叠加图层。以上的细节基本都用标志位解决，标志位设置的数量增多会导致标志位之间的冲突，所以单纯针对标志位再做一次优化，通过switch和if语句解决。这样基本功能就可以实现了，并且一些非常规操作也不会导致错误闪退。


