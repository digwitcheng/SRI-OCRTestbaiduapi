#include "Thinning.h"
//#include "DIBAPI.h"
#include <math.h>
#include<stdlib.h>


/////////////////////////////////////////////////////////////////////////
//Rosenfeld细化算法
//功能：对图象进行细化
//参数：image：代表图象的一维数组
//      lx：图象宽度
//      ly：图象高度
//      无返回值
void ThinRosenfeld(int *image, int lx, int ly)
{
	int *f, *g;
	int n[10];
	int  a[5] = {0, -1, 1, 0, 0};
	int b[5] = {0, 0, 0, 1, -1};
	int  nrnd, cond, n48, n26, n24, n46, n68, n82, n123, n345, n567, n781;
	int k, shori;
	int i, j;
	int ii, jj, kk, kk1, kk2, kk3;

	g = (int  *)malloc(lx * ly*sizeof(int));
	if(g==NULL)
	{
		//printf("error in alocating mmeory!\n");
		return;
	}

	f = image;
	memcpy(g,f,lx * ly*sizeof(int));


	do
	{
		shori = 0;
		for(k=1; k<=4; k++)
		{
			for(i=1; i<lx-1; i++)
			{
				ii = i + a[k];

				for(j=1; j<ly-1; j++)
				{
					kk = i*ly + j;

					if(!f[kk])
						continue;

					jj = j + b[k];
					kk1 = ii*ly + jj;

					if(f[kk1])
						continue;

					kk1 = kk - ly -1;
					kk2 = kk1 + 1;
					kk3 = kk2 + 1;
					n[3] = f[kk1];
					n[2] = f[kk2];
					n[1] = f[kk3];
					kk1 = kk - 1;
					kk3 = kk + 1;
					n[4] = f[kk1];
					n[8] = f[kk3];
					kk1 = kk + ly - 1;
					kk2 = kk1 + 1;
					kk3 = kk2 + 1;
					n[5] = f[kk1];
					n[6] = f[kk2];
					n[7] = f[kk3];

					nrnd = n[1] + n[2] + n[3] + n[4]
						+n[5] + n[6] + n[7] + n[8];
					if(nrnd<=1)
						continue;

					cond = 0;
					n48 = n[4] + n[8];
					n26 = n[2] + n[6];
					n24 = n[2] + n[4];
					n46 = n[4] + n[6];
					n68 = n[6] + n[8];
					n82 = n[8] + n[2];
					n123 = n[1] + n[2] + n[3];
					n345 = n[3] + n[4] + n[5];
					n567 = n[5] + n[6] + n[7];
					n781 = n[7] + n[8] + n[1];

					if(n[2]==1 && n48==0 && n567>0)
					{
						if(!cond)
							continue;
						g[kk] = 0;
						shori = 1;
						continue;
					}

					if(n[6]==1 && n48==0 && n123>0)
					{
						if(!cond)
							continue;
						g[kk] = 0;
						shori = 1;
						continue;
					}

					if(n[8]==1 && n26==0 && n345>0)
					{
						if(!cond)
							continue;
						g[kk] = 0;
						shori = 1;
						continue;
					}

					if(n[4]==1 && n26==0 && n781>0)
					{
						if(!cond)
							continue;
						g[kk] = 0;
						shori = 1;
						continue;
					}

					if(n[5]==1 && n46==0)
					{
						if(!cond)
							continue;
						g[kk] = 0;
						shori = 1;
						continue;
					}

					if(n[7]==1 && n68==0)
					{
						if(!cond)
							continue;
						g[kk] = 0;
						shori = 1;
						continue;
					}

					if(n[1]==1 && n82==0)
					{
						if(!cond)
							continue;
						g[kk] = 0;
						shori = 1;
						continue;
					}

					if(n[3]==1 && n24==0)
					{
						if(!cond)
							continue;
						g[kk] = 0;
						shori = 1;
						continue;
					}

					cond = 1;
					if(!cond)
						continue;
					g[kk] = 0;
					shori = 1;
				}
			}

			for(i=0; i<lx; i++)
			{
				for(j=0; j<ly; j++)
				{
					kk = i*ly + j;
					f[kk] = g[kk];
				}
			}
		}
	}while(shori);

	free(g);
}					


void expanding(int* image,int lWidth, int lHeight)
{
	int i,j,m,n;
	int pixel;
	for(i=0;i<lHeight;i++)
	{
		for(j=0;j<lWidth;j++)
		{
			pixel=image[lWidth*i+j];
			if(pixel==0)
			{
				for(m=0;m<3;m++)
				{
					for(n=0;n<3;n++)
					{
						pixel=image[lWidth*(i+m-1)+(j+n-1)];
						if(pixel==255)
							image[lWidth*(i+m-1)+(j+n-1)]=1;
					}
				}
			}
		}
	}
	for(i=0;i<lHeight;i++)
	{
		for(j=0;j<lWidth;j++)
		{
			pixel=image[lWidth*i+j];
			if(pixel==1)
				image[lWidth*i+j]=0;
		}
	}
				
}

