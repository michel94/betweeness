
import matplotlib.pyplot as plt
from math import log

def showGraph(path):
	with open(path, 'r') as f:
		content = f.read()
		
		data = []

		for i in content.split(", "):
			try:
				data.append(float(i))
			except ValueError:
				print(i)

		points = [ (log(k+1), log(v+0.001)) for k, v in enumerate(data)]

		plt.plot(*zip(*points))
		plt.ylabel('Sorted point ids')
		plt.ylabel('Betweenness value')
		plt.show()


wiki = "datasets/eswiki-2013.bw"
hol = "datasets/hollywood-2009.bw"

showGraph(hol)

