#include "processing.h"
#include <math.h>
#include<string.h>

//字符分割
CRect CharSegment(int * proImage, int lWidth, int lHeight)
{
	
//清空用来保存每个字符区域的链表
	CRect charRect1;
	charRect1.left=0;
	charRect1.right=lWidth-1;
	charRect1.top=0;
	charRect1.bottom=lHeight-1;
    //象素的灰度值
    int gray; 

	//设置循环变量
	int i,j;
	//从上往下扫描，找到上边界

	//行
	for (i=0;i<lHeight;i++)
	{
         //列
  		for (j=0;j<lWidth;j++)
		{
            
			//获得该点的灰度值
			gray = proImage[i*lWidth+j];

			//看是否为黑点
			if (gray == 0)
			{   
               //若为黑点，把此点作为字符大致的最高点
				charRect1.top = (i==0)?i:i-1;

				//对i强行赋值以中断循环
				i=lHeight;

				//跳出循环
				break;
			}

        //如果该点不是黑点，继续循环
		}
	}


    //从下往上扫描，找下边界

	//行
	for (i = lHeight-1;i>=0;i--)
    {

		//列
		for (j=0;j<lWidth;j++)
		{
			
			//获取该点的灰度值
			gray = proImage[i*lWidth+j];


			//判断是否为黑点
			if (gray == 0)
			{
				//若为黑点，把此点作为字符大致的最低点
				charRect1.bottom =(i==lHeight-1)?i: i+1;

				//对i强行赋值以中断循环
				i=-1;

				//跳出循环
				break;
			}

          //如果该点不是黑点，继续循环
		}
	
	}

   
	//行
	for (i=0;i<lWidth;i++)
	{
		for (j=0;j<lHeight;j++)
		{	
			//获取该点的灰度值
			gray = proImage[lWidth*j+i];

            //判断是否为黑点
			if (gray == 0)
			{
				charRect1.left = (i==0)?i:i-1;

				i=lWidth;
				break;
				
			}		
		}
	}
	for (i=lWidth-1;i>=0;i--)
	{
		for (j=0;j<lHeight;j++)
		{	
			//获取该点的灰度值
			gray = proImage[lWidth*j+i];

            //判断是否为黑点
			if (gray == 0)
			{
				charRect1.right = (i==lWidth-1)?i:i+1;

				i=-1;
				break;
				
			}		
		}
	}

	//将链表1返回
	return charRect1;
}


//归一化处理
void StdDIBbyRect(int* proImage,CRect &m_charRect,int lWidth,int lHeight,int tarWidth, int tarHeight)
{	

	// 循环变量
	int	i;
	int	j;
	
	//宽度、高度方向上的缩放因子
	double wscale,hscale;

	//开辟一块临时缓存区,来存放变化后的图像信息
	int image[200*200];
	memset(image,255,lWidth*lHeight*sizeof(int));
    
	//进行映射操作的坐标变量
	int i_src,j_src;

	//存放字符位置信息的结构体
	CRect rect;
	CRect rectnew;

	rect= m_charRect;

	//计算缩放因子

	//横坐标方向的缩放因子
	wscale=(double)tarWidth/(rect.right-rect.left+1);
	//纵坐标方向的缩放因子
	hscale=(double)tarHeight/(rect.bottom-rect.top+1);

	//计算标准化矩形

	//上边界
	rectnew.top =rect.top ;

	//下边界
	rectnew.bottom =rect.top +tarHeight;

	//左边界
	rectnew.left =rect.left ;

	//右边界
	rectnew.right =rectnew.left +tarWidth;

	//将原矩形框内的象素映射到新的矩形框内
	for(i=rectnew.top ;i<rectnew.bottom ;i++)
	{
		for(j=rectnew.left ;j<rectnew.right ;j++)
		{   
			//计算映射坐标
			i_src=rectnew.top +(int)((i-rectnew.top )/hscale);
			j_src=rectnew.left +(int)((j-rectnew.left )/wscale);

			//将相对应的象素点进行映射操作
			
			image[lWidth*i+j]=proImage[lWidth*i_src+j_src];
		}
	}
	
    m_charRect=rectnew;
	//将缓存区的内容拷贝到图像的数据区内
	memcpy(proImage,image,lHeight*lWidth*sizeof(int));

	
}

//删除离散点
void RemoveScatterNoise(int* image, int lWidth,int lHeight,int &m_lianxushu)
{
	//设置判定噪声的长度阈值为15
	//即如果与考察点相连接的黑点的数目小于15则认为考察点是噪声点
	int length=8;
	
	// 循环变量
	m_lianxushu=0;
	int	i;
	int	j;	
	int k;

	//开辟一块用来存放标志的内存数组
//	int *lplab = new int[lHeight * lWidth*sizeof(int)];
	int *lplab;
	lplab=(int*)malloc(lHeight * lWidth*sizeof(int));

	//开辟一块用来保存离散判定结果的内存数组
	//bool *lpTemp = new bool[lHeight * lWidth*sizeof(bool)];
	bool *lpTemp;
	lpTemp=(bool*)malloc(lHeight * lWidth*sizeof(bool));

    //初始化标志数组
	for (i=0;i<lHeight*lWidth;i++)
    {

    //将所有的标志位设置为非
	lplab[i] = 0;

	}

	//用来存放离散点的坐标的数组
	int lab[25];
	
	//为循环变量赋初始值
	k=0;

	//扫描整个图像

	//逐行扫描
	for(i =0;i<lHeight;i++)
	{  
       
	   //逐行扫描
		for(j=0;j<lWidth;j++)
			{	
				//先把标志位置false
				for(k=0;k<m_lianxushu;k++)
				lplab[lab[k]] = 0;

				//连续数置0
				m_lianxushu =0;

			    //进行离散性判断
			    lpTemp[i*lWidth+j] = DeleteScaterJudge(image,lplab,lWidth,lHeight,j,i,lab,m_lianxushu,length);

			}
	}
			
	//扫描整个图像，把离散点填充成白色

	//逐行扫描
	for(i = 0;i<lHeight;i++)
	{

      //逐列扫描
		for(j=0;j<lWidth;j++)
		{       
			    //查看标志位,如果为非则将此点设为白点
				if(lpTemp[i*lWidth+j] == false)
				{	
                    //将此象素设为白点
					image[i*lWidth+j]=255;
					
				}
			}
	}
//	delete []lplab;
//	delete []lpTemp;
	free(lplab);
	free(lpTemp);

}
bool DeleteScaterJudge(int* image,int* lplab, int lWidth, int lHeight, int x, int y, int lab[], int& m_lianxushu,int lianXuShu)
{
	
	//如果连续长度满足要求，说明不是离散点，返回
	if(m_lianxushu>=lianXuShu)
		return true;

	//长度加一
	m_lianxushu++;

	//设定访问标志
	lplab[lWidth * y +x] = 1;
	
	//保存访问点坐标
//	lab[m_lianxushu-1].x = x;
	//lab[m_lianxushu-1].y = y;
	lab[m_lianxushu-1]=y*lWidth+x;
	//象素的灰度值
	int gray;
  
	//长度判定
    //如果连续长度满足要求，说明不是离散点，返回
	if(m_lianxushu>=lianXuShu)
		return true;
	
	//下面进入递归
	else
	{	
		//考察上下左右以及左上、右上、左下、右下八个方向
		//如果是黑色点，则调用函数自身进行递归

		//考察下面点
		
		//传递灰度值
		gray=image[(y-1)*lWidth+x];

		//如果点在图像内、颜色为黑色并且没有被访问过
		if(y-1 >=0 && gray == 0 && lplab[(y-1)*lWidth+x] == 0)

		//进行递归处理		
		DeleteScaterJudge(image,lplab,lWidth,lHeight,x,y-1,lab,m_lianxushu,lianXuShu);

		//判断长度
		//如果连续长度满足要求，说明不是离散点，返回
		if(m_lianxushu>=lianXuShu)
		return true;
		
		//左下点
		
        //传递灰度值
		gray=image[(y-1)*lWidth+x-1];

        //如果点在图像内、颜色为黑色并且没有被访问过
		if(y-1 >=0 &&  x-1 >=0 && gray== 0 && lplab[(y-1)*lWidth+x-1] == 0)

      	//进行递归处理		
		DeleteScaterJudge(image,lplab,lWidth,lHeight,x-1,y-1,lab,m_lianxushu,lianXuShu);

        //判断长度
		//如果连续长度满足要求，说明不是离散点，返回
		if(m_lianxushu>=lianXuShu)
		return true;
		
		//左边
		
		//传递灰度值
		gray=image[y*lWidth+x-1];

        //如果点在图像内、颜色为黑色并且没有被访问过
		if(x-1 >=0 &&  gray== 0 && lplab[y*lWidth+x-1] == 0)

        //进行递归处理		
		DeleteScaterJudge(image,lplab,lWidth,lHeight,x-1,y,lab,m_lianxushu,lianXuShu);

        //判断长度
		//如果连续长度满足要求，说明不是离散点，返回
		if(m_lianxushu>=lianXuShu)
			return true;
		
		//左上
		
		//传递灰度值
		gray=image[lWidth*(y+1)+x-1];

        //如果点在图像内、颜色为黑色并且没有被访问过
		if(y+1 <lHeight && x-1 >= 0 && gray == 0 && lplab[(y+1)*lWidth+x-1] == 0)

		//进行递归处理
		
		DeleteScaterJudge(image,lplab,lWidth,lHeight,x-1,y+1,lab,m_lianxushu,lianXuShu);

        //判断长度
		//如果连续长度满足要求，说明不是离散点，返回
		if(m_lianxushu>=lianXuShu)
			return true;
		
		//上面
	
        //传递灰度值
		gray=image[lWidth*(y+1)+x];

        //如果点在图像内、颜色为黑色并且没有被访问过
		if(y+1 < lHeight && gray == 0 && lplab[(y+1)*lWidth+x] == 0)

        //进行递归处理
		
		DeleteScaterJudge(image,lplab,lWidth,lHeight,x,y+1,lab,m_lianxushu,lianXuShu);

        //判断长度
		//如果连续长度满足要求，说明不是离散点，返回
		if(m_lianxushu>=lianXuShu)
			return true;
		
		//右上
		
		//传递灰度值
		gray=image[lWidth*(y+1)+x+1];

        //如果点在图像内、颜色为黑色并且没有被访问过
		if(y+1 <lHeight && x+1 <lWidth &&  gray == 0 && lplab[(y+1)*lWidth+x+1] == 0)

        //进行递归处理
		DeleteScaterJudge(image,lplab,lWidth,lHeight,x+1,y+1,lab,m_lianxushu,lianXuShu);

        //判断长度
		//如果连续长度满足要求，说明不是离散点，返回
		if(m_lianxushu>=lianXuShu)
			return true;
		
		//右边
	
        //传递灰度值
		gray=image[lWidth*y+x+1];

		//如果点在图像内、颜色为黑色并且没有被访问过
		if(x+1 <lWidth && gray==0 && lplab[y*lWidth+x+1] == 0)

        //进行递归处理		
		DeleteScaterJudge(image,lplab,lWidth,lHeight,x+1,y,lab,m_lianxushu,lianXuShu);

        //判断长度

		//如果连续长度满足要求，说明不是离散点，返回
		if(m_lianxushu>=lianXuShu)
			return true;
		
		//右下
		
        //传递灰度值
		gray=image[lWidth*(y-1)+x+1];

        //如果点在图像内、颜色为黑色并且没有被访问过
		if(y-1 >=0 && x+1 <lWidth && gray == 0 && lplab[(y-1)*lWidth+x+1] == 0)

       //进行递归处理		
	   DeleteScaterJudge(image,lplab,lWidth,lHeight,x+1,y-1,lab,m_lianxushu,lianXuShu);

        //判断长度
		//如果连续长度满足要求，说明不是离散点，返回
		if(m_lianxushu>=lianXuShu)
			return true;
	}
	

	//如果递归结束，返回false，说明是离散点
	return false;

}
